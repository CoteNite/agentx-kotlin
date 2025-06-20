package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.conversation.dto.MessageDTO
import cn.cotenite.agentxkotlin.domain.conversation.repository.MessageRepository
import cn.cotenite.agentxkotlin.domain.llm.model.LlmMessage
import cn.cotenite.agentxkotlin.domain.llm.model.LlmRequest
import cn.cotenite.agentxkotlin.domain.llm.service.LlmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:32
 */
@Service
class ConversationDomainService(
    private val sessionDomainService: SessionDomainService,
    private val contextService: ContextService,
    private val messageService: MessageService,
    private val llmService: LlmService,
    private val messageRepository: MessageRepository,
){

    companion object{
        private const val DEFAULT_SYSTEM_PROMPT: String = "你是一个有帮助的AI助手，请尽可能准确、有用地回答用户问题。"
    }

    private val log= LoggerFactory.getLogger(ConversationDomainService::class.java)


    fun chat(sessionId: String, content: String): SseEmitter {
        val emitter = SseEmitter()
        messageService.sendUserMessage(sessionId, content)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val contextMessages = contextService.getContextMessages(sessionId)
                val llmMessages = this@ConversationDomainService.convertToLlmMessages(contextMessages)

                val request = LlmRequest(
                    messages = llmMessages,
                    stream = true
                )

                val fullResponse  = StringBuilder()

                llmService.chatStreamList(request)
                    .catch { e ->
                        log.error("Flow处理异常", e)
                        emit("流处理异常: ${e.message}")
                    }
                    .collect{chunk->
                        fullResponse.append(chunk)
                        val response = StreamChatResponse(
                            content=chunk,
                            done = false,
                            sessionId = sessionId,
                            provider = llmService.getProviderName(),
                            model = llmService.getDefaultModel(),
                        )
                        try {
                            emitter.send(response)
                        } catch (e: IOException) {
                            log.error("发送SSE响应失败", e)
                            emitter.completeWithError(e)
                            return@collect // 停止收集
                        }
                    }
                val assistantMessageDTO = messageService.saveAssistantMessage(
                    sessionId = sessionId,
                    content=fullResponse.toString(),
                    provider = llmService.getProviderName(),
                    model = llmService.getDefaultModel(),
                    tokenCount = 0
                )

                val doneResponse = StreamChatResponse(
                    content = "",
                    done = true,
                    sessionId = sessionId,
                    provider = llmService.getProviderName(),
                    model =llmService.getDefaultModel()
                )

                emitter.send(doneResponse)
                emitter.complete()

            }catch (e:Exception){
                log.error("Stream chat error", e)
                try {
                    val errorResponse = StreamChatResponse(
                        content="错误: ${e.message}",
                        done = true,
                        sessionId = sessionId,
                        provider = "",
                        model = ""
                    )
                    emitter.send(errorResponse)
                    emitter.complete()
                } catch (ex: IOException) {
                    emitter.completeWithError(ex)
                }
            }
        }
        return emitter
    }

    fun chatSync(sessionId: String, content: String): MessageDTO {
        messageService.sendUserMessage(sessionId, content)

        val contextMessages = contextService.getContextMessages(sessionId)

        val llmMessages: MutableList<LlmMessage> = this.convertToLlmMessages(contextMessages)

        val request = LlmRequest()
        request.messages=llmMessages

        val response = llmService.chat(request).content

        return messageService.saveAssistantMessage(
            sessionId,
            response,
            llmService.getProviderName(),
            llmService.getDefaultModel(),
            0
        )
    }

    private fun convertToLlmMessages(messageEntities: MutableList<MessageEntity>): MutableList<LlmMessage> {
        val llmMessages = mutableListOf<LlmMessage>()
        for (message in messageEntities) {
            llmMessages.add(LlmMessage(message.role, message.content))
        }
        return llmMessages

    }



    fun createSessionAndChat(title: String, userId: String, content: String): SseEmitter {

        val sessionDTO = sessionDomainService.createSession(title, userId, "")
        val sessionId = sessionDTO.id

        messageService.saveSystemMessage(sessionId, DEFAULT_SYSTEM_PROMPT)

        return this.chat(sessionId, content)
    }

    fun clearContext(sessionId: String) {
        contextService.clearContext(sessionId)
    }

    fun deleteConversationMessages(sessionIds: List<String>) {
        messageRepository.deleteBySessionIdIn(sessionIds)
    }

}
