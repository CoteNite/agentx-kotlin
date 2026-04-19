package cn.cotenite.application.llm.assembler

import cn.cotenite.application.llm.dto.ModelDTO
import cn.cotenite.application.llm.dto.ProviderDTO
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderAggregate
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.interfaces.dto.llm.request.ProviderCreateRequest
import cn.cotenite.interfaces.dto.llm.request.ProviderUpdateRequest
import java.time.LocalDateTime

/** 服务提供商对象转换器 */
object ProviderAssembler {

    /** 将实体转换为DTO，并进行敏感信息脱敏 */
    fun toDTO(provider: ProviderEntity?): ProviderDTO? {
        if (provider == null) {
            return null
        }

        val dto = ProviderDTO()
        dto.id = provider.id
        dto.protocol = provider.protocol
        dto.name = provider.name
        dto.description = provider.description
        dto.config = provider.config
        dto.isOfficial = provider.isOfficial
        dto.status = provider.status
        dto.createdAt = provider.createdAt
        dto.updatedAt = provider.updatedAt

        // 脱敏处理（针对返回前端的场景）
        dto.maskSensitiveInfo()

        return dto
    }

    /** 将多个聚合根转换为DTO列表 */
    fun toDTOList(providers: List<ProviderAggregate>): List<ProviderDTO> {
        return providers.map { toDTO(it) }.filterNotNull()
    }

    /** 将多个实体转换为DTO列表 */
    fun toDTOListFromEntities(providers: List<ProviderEntity>): List<ProviderDTO> {
        return providers.map { toDTO(it) }.filterNotNull()
    }

    /** 将创建请求转换为实体 */
    fun toEntity(request: ProviderCreateRequest, userId: String): ProviderEntity {
        val provider = ProviderEntity()
        provider.userId = userId
        provider.protocol = request.protocol
        provider.name = request.name
        provider.description = request.description
        provider.config = request.config // 会自动加密
        provider.status = request.status!!
        provider.createdAt = LocalDateTime.now()
        provider.updatedAt = LocalDateTime.now()
        return provider
    }

    /** 将更新请求转换为实体 */
    fun toEntity(request: ProviderUpdateRequest, userId: String): ProviderEntity {
        val provider = ProviderEntity()
        provider.id = request.id
        provider.userId = userId
        provider.protocol = request.protocol
        provider.name = request.name
        provider.description = request.description
        provider.config = request.config // 会自动加密
        provider.status = request.status!!
        provider.updatedAt = LocalDateTime.now()
        return provider
    }

    /** 根据更新请求更新实体 */
    fun updateEntity(entity: ProviderEntity?, request: ProviderUpdateRequest?) {
        if (entity == null || request == null) {
            return
        }

        if (request.name != null) {
            entity.name = request.name
        }

        if (request.description != null) {
            entity.description = request.description
        }

        if (request.config != null) {
            entity.config = request.config // 会自动加密
        }

        if (request.status != null) {
            entity.status = request.status!!
        }

        entity.updatedAt = LocalDateTime.now()
    }

    // 将聚合根转换为dto
    fun toDTO(provider: ProviderAggregate?): ProviderDTO? {
        if (provider == null) {
            return null
        }
        val dto = toDTO(provider.entity) ?: return null

        val models = provider.getModels()
        if (models == null || models.isEmpty()) {
            return dto
        }
        for (model in models) {
            val modelDTO = ModelAssembler.toDTO(model, provider.getName()) ?: continue
            modelDTO.isOfficial = provider.getIsOfficial()
            dto.models.add(modelDTO)
        }
        return dto
    }
}
