package cn.cotenite.application.llm.dto

import cn.cotenite.domain.llm.model.enums.ModelType
import java.time.LocalDateTime

/**
 * 模型数据传输对象
 */
data class ModelDTO(
    var id: String? = null,
    var userId: String? = null,
    var providerId: String? = null,
    var providerName: String? = null,
    var modelId: String? = null,
    var name: String? = null,
    var description: String? = null,
    var type: ModelType? = null,
    var isOfficial: Boolean? = null,
    var status: Boolean? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
)
