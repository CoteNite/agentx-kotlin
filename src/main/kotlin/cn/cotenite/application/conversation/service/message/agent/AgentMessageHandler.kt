package cn.cotenite.application.conversation.service.message.agent

import cn.cotenite.application.conversation.service.message.AbstractMessageHandler
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import dev.langchain4j.service.tool.ToolProvider
import org.springframework.stereotype.Component

/**
 * @author  yhk
 * Description  
 * Date  2026/3/29 19:14
 */
@Component(value = "agentMessageHandler")
class AgentMessageHandler(
    private val agentToolManager:AgentToolManager,
    override val llmServiceFactory: LLMServiceFactory,
    override val messageDomainService: MessageDomainService
): AbstractMessageHandler(llmServiceFactory,messageDomainService) {


    // todo xhy 从 agent 中获取
    override fun provideTools(): ToolProvider {
        return agentToolManager.createToolProvider(
            agentToolManager.getAvailableTools()
        )
    }

}