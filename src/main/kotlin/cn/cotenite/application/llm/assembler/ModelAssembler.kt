package cn.cotenite.application.llm.assembler

import cn.cotenite.application.llm.dto.ModelDTO
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.enums.ModelType
import cn.cotenite.interfaces.dto.llm.request.ModelCreateRequest
import cn.cotenite.interfaces.dto.llm.request.ModelUpdateRequest
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
            modelEndpoint = it.modelEndpoint,
            status = it.status,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt,
            isOfficial = it.isOfficial
        )
    }

    /** 将领域对象转换为DTO，并设置服务商名称  */
    fun toDTO(model: ModelEntity?, providerName: String?): ModelDTO? {
        val dto = toDTO(model)
        if (dto != null) {
            dto.providerName=providerName
        }
        return dto
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
            if (request.modelEndpoint!=null && request.modelEndpoint!!.isEmpty()){
                modelEndpoint=request.modelEndpoint
            }else{
                modelEndpoint=request.modelId
            }
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
        if (request.modelEndpoint!=null && request.modelEndpoint!!.isEmpty()){
            modelEndpoint=request.modelEndpoint
        }else{
            modelEndpoint=request.modelId
        }
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
