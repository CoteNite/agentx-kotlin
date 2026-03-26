package cn.cotenite.domain.conversation.handler

import org.springframework.stereotype.Component
import cn.cotenite.domain.conversation.constant.Role
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.service.ContextDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import cn.cotenite.infrastructure.transport.MessageTransport

/**
 * 标准消息处理器 - 使用 coroutines 和 Flow
 */
@Component("standardMessageHandler")
open class StandardMessageHandler(
    private val conversationDomainService: ConversationDomainService,
    private val contextDomainService: ContextDomainService,
    private val llmServiceFactory: LLMServiceFactory
) : MessageHandler {

    override fun <T> handleChat(environment: ChatEnvironment, transport: MessageTransport<T>): T {
        val userMessageEntity = MessageEntity().apply {
            role = Role.USER
            content = environment.userMessage
            sessionId = environment.sessionId
        }
        val llmMessageEntity = MessageEntity().apply {
            role = Role.SYSTEM
            sessionId = environment.sessionId
            model = environment.model.modelId
            provider = environment.provider.id
        }

        val connection = transport.createConnection(300_000L)

        // 构建 Flow 并交由 transport 以 coroutine 消费
        val responseFlow = llmServiceFactory.getStreamingFlow(environment, userMessageEntity, llmMessageEntity)
        transport.handleStreamingResponse(connection, responseFlow, environment, userMessageEntity, llmMessageEntity)

        return connection
    }
}
