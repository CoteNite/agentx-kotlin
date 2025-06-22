package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.agent.model.AgentModelConfig
import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import jakarta.validation.constraints.NotBlank

/**
 * 更新Agent信息的请求对象
 * 整合了基本信息和配置信息
 */
data class UpdateAgentRequest(
    var agentId: String,

    @field:NotBlank(message = "助理名称不可为空")
    var name: String,

    var avatar: String,
    var description: String,
    var enabled: Boolean,
    
    var systemPrompt: String,
    var welcomeMessage: String,
    var modelConfig: AgentModelConfig,
    var tools: List<AgentTool>,
    var knowledgeBaseIds: List<String>
)