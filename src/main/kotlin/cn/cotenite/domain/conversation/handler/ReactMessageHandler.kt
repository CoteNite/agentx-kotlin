package cn.cotenite.domain.conversation.handler

import org.springframework.stereotype.Component
import cn.cotenite.domain.conversation.service.ContextDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import cn.cotenite.infrastructure.transport.MessageTransport

/**
 * React消息处理器
 */
@Component("reactMessageHandler")
class ReactMessageHandler(
    conversationDomainService: ConversationDomainService,
    contextDomainService: ContextDomainService,
    llmServiceFactory: LLMServiceFactory
) : StandardMessageHandler(conversationDomainService, contextDomainService, llmServiceFactory) {

    override fun <T> handleChat(environment: ChatEnvironment, transport: MessageTransport<T>): T {
        return super.handleChat(environment, transport)
    }

    protected fun invokeExternalTool(toolName: String, parameters: Any?): Any? = null
}
