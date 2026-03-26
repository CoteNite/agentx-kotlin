package cn.cotenite.domain.llm.model

import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import java.time.LocalDateTime

/**
 * 服务商聚合根
 */
class ProviderAggregate(
    var entity: ProviderEntity,
    models: List<ModelEntity>?
) {
    private var _models: MutableList<ModelEntity> = models?.toMutableList() ?: mutableListOf()

    fun addModel(model: ModelEntity?) {
        model?.takeIf { it.providerId == entity.id }?.let(_models::add)
    }

    fun setModels(models: List<ModelEntity>?) {
        _models = models?.toMutableList() ?: mutableListOf()
    }

    fun getModels(): List<ModelEntity> = _models
    fun getConfig(): ProviderConfig? = entity.config
    fun setConfig(config: ProviderConfig?) { entity.config = config }

    fun getId(): String? = entity.id
    fun getUserId(): String? = entity.userId
    fun getProtocol(): ProviderProtocol? = entity.protocol
    fun setProtocol(protocol: ProviderProtocol?) { entity.protocol = protocol }

    fun getName(): String? = entity.name
    fun setName(name: String?) { entity.name = name }

    fun getDescription(): String? = entity.description
    fun setDescription(description: String?) { entity.description = description }

    fun getIsOfficial(): Boolean = entity.isOfficial
    fun setIsOfficial(isOfficial: Boolean) { entity.isOfficial = isOfficial }

    fun getStatus(): Boolean = entity.status
    fun setStatus(status: Boolean) { entity.status = status }

    fun getCreatedAt(): LocalDateTime? = entity.createdAt
    fun getUpdatedAt(): LocalDateTime? = entity.updatedAt
    fun getDeletedAt(): LocalDateTime? = entity.deletedAt
}
