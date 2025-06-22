package cn.cotenite.agentxkotlin.application.conversation.service

import cn.cotenite.agentxkotlin.application.conversation.assmebler.MessageAssembler
import cn.cotenite.agentxkotlin.application.conversation.dto.ChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.MessageDTO
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import cn.cotenite.agentxkotlin.domain.agent.service.AgentDomainService
import cn.cotenite.agentxkotlin.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.agentxkotlin.domain.conversation.constant.Role
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.conversation.service.ConversationDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.SessionDomainService
import cn.cotenite.agentxkotlin.domain.llm.service.LlmDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.infrastructure.llm.LLMProviderService
import cn.cotenite.agentxkotlin.infrastructure.llm.config.ProviderConfig
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

/**
 * 对话应用服务，用于适配域层的对话服务
 */
@Service
class ConversationAppService(
    private val conversationDomainService: ConversationDomainService,
    private val sessionDomainService: SessionDomainService,
    private val agentDomainService: AgentDomainService,
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val llmDomainService: LlmDomainService,
    private val llmProviderService: LLMProviderService
) {

    /**
     * 获取会话中的消息列表
     *
     * @param sessionId 会话id
     * @param userId    用户id
     * @return 消息列表
     */
    fun getConversationMessages(sessionId: String, userId: String): List<MessageDTO> {
        // 查询对应会话是否存在
        val sessionEntity = sessionDomainService.find(sessionId, userId)
            ?: throw BusinessException("会话不存在")

        val conversationMessages = conversationDomainService.getConversationMessages(sessionId)
        return MessageAssembler.toDTOs(conversationMessages)
    }

    fun chat(chatRequest: ChatRequest, userId: String): SseEmitter {
        // 获取会话
        val sessionId = chatRequest.sessionId
        val session = sessionDomainService.getSession(sessionId, userId)
        val agentId = session.agentId?:throw BusinessException("会话不存在")

        // 获取对应agent是否可以使用：如果 userId 不同并且是禁用，则不可对话
        val agent = agentDomainService.getAgentById(agentId)
        if (agent.userId != userId && !agent.enabled) {
            throw BusinessException("agent已被禁用")
        }

        // 从工作区中获取对应的模型信息
        val workspace = agentWorkspaceDomainService.getWorkspace(agentId, userId)
        val modelId = workspace.modelId?: throw BusinessException("未指定模型")
        val model = llmDomainService.getModelById(modelId)

        model.isActive()

        // 获取服务商信息
        val provider = llmDomainService.getProvider(model.providerId?:throw BusinessException("未指定服务商"), userId)
        provider.isActive()

        // 对话 todo 这里需要传入消息列表 ，并且目前默认流式
        val config = provider.config?:throw BusinessException("未指定服务商配置")
        val chatStreamClient = llmProviderService.getStream(
            provider.protocol?: ProviderProtocol.OpenAI,
            ProviderConfig(config.apiKey, config.baseUrl, model.modelId?:throw BusinessException("未指定模型"), emptyMap())
        )

        // 用户消息
        val userMessageEntity = MessageEntity().apply {
            role = Role.USER
            content = chatRequest.message
            this.sessionId = sessionId
        }

        // 大模型消息
        val llmMessageEntity = MessageEntity().apply {
            role = Role.SYSTEM
            this.sessionId = sessionId
            this.model = model.modelId
            this.provider = provider.id
        }

        val emitter = SseEmitter()

        chatStreamClient?.chat(chatRequest.message, object : StreamingChatResponseHandler {
            override fun onPartialResponse(partialResponse: String) {
                try {
                    val response = StreamChatResponse(
                        content = partialResponse,
                        done = false,
                        sessionId = sessionId,
                        provider = provider.name?:throw BusinessException("Provider not found"),
                        model = model.modelId?: throw BusinessException("Model not found")
                    )
                    emitter.send(response)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }

            override fun onCompleteResponse(completeResponse: ChatResponse) {
                // todo 传出去
                val tokenUsage = completeResponse.metadata().tokenUsage()

                val inputTokenCount = tokenUsage.inputTokenCount()
                userMessageEntity.tokenCount = inputTokenCount
                val outputTokenCount = tokenUsage.outputTokenCount()
                llmMessageEntity.tokenCount = outputTokenCount
                llmMessageEntity.content = completeResponse.aiMessage().text()
                
                try {
                    val response = StreamChatResponse(
                        content = "",
                        done = true,
                        sessionId = sessionId,
                        provider = provider.name?:throw BusinessException("Provider not found"),
                        model = model.modelId?: throw BusinessException("Model not found")
                    )
                    emitter.send(response)
                    emitter.complete()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
                
                conversationDomainService.insertBatchMessage(listOf(userMessageEntity, llmMessageEntity))
            }

            override fun onError(error: Throwable) {
                try {
                    val response = StreamChatResponse(
                        content = error.message ?: "Unknown error",
                        done = true,
                        sessionId = sessionId,
                        provider = "",
                        model = ""
                    )
                    emitter.send(response)
                    emitter.complete()
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
        })

        return emitter
    }
}