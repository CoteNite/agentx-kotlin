package cn.cotenite.application.llm.assembler

import cn.cotenite.application.llm.dto.ModelDTO
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.enums.ModelType
import cn.cotenite.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.interfaces.dto.llm.ModelUpdateRequest
import java.time.LocalDateTime

/**
 * 模型对象转换器
 */
object ModelAssembler {

    fun toDTO(model: ModelEntity?): ModelDTO? = model?.let {
        ModelDTO(
            id = it.id,
            userId = it.userId,
            providerId = it.providerId,
            providerName = null,
            modelId = it.modelId,
            name = it.name,
            description = it.description,
            type = it.type,
            status = it.status,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt,
            isOfficial = it.isOfficial
        )
    }

    fun toDTOs(models: List<ModelEntity>?): List<ModelDTO> = models.orEmpty().mapNotNull(::toDTO)

    fun toEntity(request: ModelCreateRequest, userId: String): ModelEntity {
        val now = LocalDateTime.now()
        return ModelEntity().apply {
            this.userId = userId
            providerId = request.providerId
            modelId = request.modelId
            name = request.name
            description = request.description
            type = request.type
            createdAt = now
            updatedAt = now
        }
    }

    fun toEntity(request: ModelUpdateRequest, userId: String): ModelEntity = ModelEntity().apply {
        id = request.id
        this.userId = userId
        name = request.name
        description = request.description
        modelId = request.modelId
        updatedAt = LocalDateTime.now()
    }

    fun toDTO(model: Map<String, Any?>?): ModelDTO? = model?.let {
        val modelType = when (val rawType = it["type"]) {
            is ModelType -> rawType
            is String -> ModelType.entries.firstOrNull { type -> type.name == rawType }
            else -> null
        }
        ModelDTO(
            id = it["id"] as? String,
            userId = it["userId"] as? String,
            providerId = it["providerId"] as? String,
            providerName = it["providerName"] as? String,
            modelId = it["modelId"] as? String,
            name = it["name"] as? String,
            description = it["description"] as? String,
            type = modelType,
            status = it["status"] as? Boolean,
            createdAt = it["createdAt"] as? LocalDateTime,
            updatedAt = it["updatedAt"] as? LocalDateTime,
            isOfficial = it["isOfficial"] as? Boolean
        )
    }
}
