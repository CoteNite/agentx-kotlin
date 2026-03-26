package cn.cotenite.interfaces.dto.llm

import jakarta.validation.constraints.NotBlank
import cn.cotenite.domain.llm.model.enums.ModelType

/**
 * 模型创建请求
 */
data class ModelCreateRequest(
    var providerId: String? = null,
    @field:NotBlank(message = "模型id不可为空")
    var modelId: String? = null,
    @field:NotBlank(message = "名称不可为空")
    var name: String? = null,
    var description: String? = null,
    var type: ModelType? = null
)
