package cn.cotenite.interfaces.dto.agent.request

import cn.cotenite.domain.agent.model.LLMModelConfig
import jakarta.validation.constraints.NotBlank

/**
 * 更新Agent信息的请求对象
 */
data class UpdateAgentRequest(
    var id: String? = null,
    @field:NotBlank(message = "助理名称不可为空")
    var name: String? = null,
    var avatar: String? = null,
    var description: String? = null,
    var enabled: Boolean? = null,
    var systemPrompt: String? = null,
    var welcomeMessage: String? = null,
    var modelConfig: LLMModelConfig? = null,
    var toolIds: List<String>? = null,
    var knowledgeBaseIds: List<String>? = null,
    var toolPresetParams: MutableMap<String?, MutableMap<String?, MutableMap<String?, String?>?>?>? = null,
    var multiModal: Boolean
)
