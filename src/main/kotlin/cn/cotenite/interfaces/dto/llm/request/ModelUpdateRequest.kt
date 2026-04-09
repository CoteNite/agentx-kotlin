package cn.cotenite.interfaces.dto.llm.request

import jakarta.validation.constraints.NotBlank

/**
 * 模型更新请求
 */
data class ModelUpdateRequest(
    var id: String? = null,
    @field:NotBlank(message = "模型id不可为空")
    var modelId: String? = null,
    @field:NotBlank(message = "名称不可为空")
    var name: String? = null,
    var description: String? = null
)
