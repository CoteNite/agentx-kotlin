package cn.cotenite.interfaces.dto.agent.request

import jakarta.validation.constraints.NotBlank
import cn.cotenite.domain.agent.constant.AgentType
import cn.cotenite.domain.agent.model.AgentTool

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
    var toolIds: List<String>? = null,
    var knowledgeBaseIds: List<String>? = null,
    var toolPresetParams: MutableMap<String?, MutableMap<String?, MutableMap<String?, String?>?>?>? =null,
    var multiModal: Boolean
)
