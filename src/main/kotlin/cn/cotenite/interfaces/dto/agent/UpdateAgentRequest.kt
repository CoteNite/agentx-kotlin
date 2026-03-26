package cn.cotenite.interfaces.dto.agent

import jakarta.validation.constraints.NotBlank
import cn.cotenite.domain.agent.model.AgentTool
import cn.cotenite.domain.agent.model.LLMModelConfig

/**
 * 更新Agent信息的请求对象
 */
data class UpdateAgentRequest(
    var agentId: String? = null,
    @field:NotBlank(message = "助理名称不可为空")
    var name: String? = null,
    var avatar: String? = null,
    var description: String? = null,
    var enabled: Boolean? = null,
    var systemPrompt: String? = null,
    var welcomeMessage: String? = null,
    var modelConfig: LLMModelConfig? = null,
    var tools: List<AgentTool>? = null,
    var knowledgeBaseIds: List<String>? = null
)
