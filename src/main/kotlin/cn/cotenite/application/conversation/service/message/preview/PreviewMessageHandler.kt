package cn.cotenite.application.conversation.service.message.preview

import cn.cotenite.application.conversation.dto.AgentChatResponse
import cn.cotenite.application.conversation.service.handler.context.ChatContext
import cn.cotenite.application.conversation.service.message.AbstractMessageHandler
import cn.cotenite.application.conversation.service.message.Agent
import cn.cotenite.application.conversation.service.message.agent.AgentToolManager
import cn.cotenite.domain.conversation.constant.MessageType
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import cn.cotenite.infrastructure.storage.OssUploadService
import cn.cotenite.infrastructure.transport.MessageTransport
import dev.langchain4j.service.tool.ToolProvider
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference


@Component("previewMessageHandler")
class PreviewMessageHandler(
    llmServiceFactory: LLMServiceFactory,
    messageDomainService: MessageDomainService,
    ossUploadService: OssUploadService,
    private val agentToolManager: AgentToolManager,
) : AbstractMessageHandler(llmServiceFactory, messageDomainService, ossUploadService) {

    override fun provideTools(chatContext: ChatContext): ToolProvider? {
        return agentToolManager.createToolProvider(
            agentToolManager.getAvailableTools(chatContext),
            chatContext.agent.toolPresetParams
        )
    }

    /** 预览专用的聊天处理逻辑 与正常流程的区别是不保存消息到数据库 */
    override fun <T> processChat(
        agent: Agent,
        connection: T,
        transport: MessageTransport<T>,
        chatContext: ChatContext,
        userEntity: MessageEntity,
        llmEntity: MessageEntity
    ) {
        val messageBuilder = AtomicReference(StringBuilder())

        val tokenStream = agent.chat(chatContext.userMessage)

        tokenStream
            .onError { throwable ->
                transport.sendMessage(
                    connection,
                    AgentChatResponse.buildEndMessage(throwable.message ?: "报错为空", MessageType.TEXT)
                )
            }
            .onPartialResponse { reply ->
                messageBuilder.get().append(reply)
                transport.sendMessage(connection, AgentChatResponse.build(reply, MessageType.TEXT))
            }
            .onCompleteResponse {
                transport.sendEndMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT))
            }
            .onToolExecuted { toolExecution ->
                if (messageBuilder.get().isNotEmpty()) {
                    transport.sendMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT))
                    llmEntity.content = messageBuilder.toString()
                    messageBuilder.set(StringBuilder())
                }

                val message = "执行工具：${toolExecution.request().name()}"
                val toolMessage = createLlmMessage(chatContext).apply {
                    messageType = MessageType.TOOL_CALL
                    content = message
                }

                transport.sendMessage(
                    connection,
                    AgentChatResponse.buildEndMessage(message, MessageType.TOOL_CALL)
                )
            }
            .start()
    }
}
