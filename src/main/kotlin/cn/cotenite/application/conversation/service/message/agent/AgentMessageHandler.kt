package cn.cotenite.application.conversation.service.message.agent

import cn.cotenite.application.conversation.service.handler.context.ChatContext
import cn.cotenite.application.conversation.service.message.AbstractMessageHandler
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import cn.cotenite.infrastructure.storage.OssUploadService
import dev.langchain4j.service.tool.ToolProvider
import org.springframework.stereotype.Component

@Component(value = "agentMessageHandler")
class AgentMessageHandler(
    private val agentToolManager: AgentToolManager,
    override val llmServiceFactory: LLMServiceFactory,
    override val messageDomainService: MessageDomainService,
    override val ossUploadService: OssUploadService,
) : AbstractMessageHandler(llmServiceFactory, messageDomainService, ossUploadService) {

    override fun provideTools(chatContext: ChatContext): ToolProvider {
        return agentToolManager.createToolProvider(
            agentToolManager.getAvailableTools(chatContext),
            chatContext.agent.toolPresetParams
        ) ?: throw BusinessException("无法获取工具")
    }
}
