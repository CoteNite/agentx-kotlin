package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.agent.constant.AgentType
import cn.cotenite.agentxkotlin.domain.agent.model.AgentModelConfig
import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import jakarta.validation.constraints.NotBlank

/**
 * 创建Agent的请求对象
 */
data class CreateAgentRequest(
    @field:NotBlank(message = "助理名称不可为空")
    var name: String? = null,
    var description: String? = null,
    var avatar: String? = null,
    var agentType: AgentType = AgentType.CHAT_ASSISTANT,
    var systemPrompt: String? = null,
    var welcomeMessage: String? = null,
    var modelConfig: AgentModelConfig? = null,
    var tools: List<AgentTool>? = null,
    var knowledgeBaseIds: List<String>? = null
)