package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import cn.cotenite.agentxkotlin.domain.conversation.model.Message
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageDTO
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
interface ConversationService {

    /**
     * 发送消息并流式获取回复
     *
     * @param sessionId 会话ID
     * @param content   用户消息内容
     * @return SSE事件发射器
     */
    fun chat(sessionId: String, content: String): SseEmitter

    /**
     * 发送消息并获取回复（非流式）
     *
     * @param sessionId 会话ID
     * @param content   用户消息内容
     * @return 助手回复消息
     */
    fun chatSync(sessionId: String, content: String): MessageDTO

    /**
     * 创建新会话并发送第一条消息
     *
     * @param title   会话标题
     * @param userId  用户ID
     * @param content 用户消息内容
     * @return SSE事件发射器
     */
    fun createSessionAndChat(title: String, userId: String, content: String): SseEmitter

    /**
     * 清除会话上下文
     *
     * @param sessionId 会话ID
     */
    fun clearContext(sessionId: String)

}

@Service
class ConversationServiceImpl(
    private val sessionService: SessionService,
    private val contextService: ContextService,
    private val messageService: MessageService,
    private val llmService: LlmService,
) : ConversationService {

    companion object{
        private const val DEFAULT_SYSTEM_PROMPT: String = "你是一个有帮助的AI助手，请尽可能准确、有用地回答用户问题。"
    }

    private val log= LoggerFactory.getLogger(ConversationServiceImpl::class.java)


    override fun chat(sessionId: String, content: String): SseEmitter {
        val emitter = SseEmitter()
        messageService.sendUserMessage(sessionId, content)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val contextMessages = contextService.getContextMessages(sessionId)
                val llmMessages = this@ConversationServiceImpl.convertToLlmMessages(contextMessages)

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

    override fun chatSync(sessionId: String, content: String): MessageDTO {
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

    private fun convertToLlmMessages(messages: MutableList<Message>): MutableList<LlmMessage> {
        val llmMessages = mutableListOf<LlmMessage>()
        for (message in messages) {
            llmMessages.add(LlmMessage(message.role, message.content))
        }
        return llmMessages

    }



    override fun createSessionAndChat(title: String, userId: String, content: String): SseEmitter {

        val sessionDTO = sessionService.createSession(title, userId, "")
        val sessionId = sessionDTO.id

        messageService.saveSystemMessage(sessionId, DEFAULT_SYSTEM_PROMPT)

        return this.chat(sessionId, content)
    }

    override fun clearContext(sessionId: String) {
        contextService.clearContext(sessionId)
    }

}
