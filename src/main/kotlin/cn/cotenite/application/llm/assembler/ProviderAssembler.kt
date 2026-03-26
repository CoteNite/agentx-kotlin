package cn.cotenite.application.llm.assembler

import cn.cotenite.application.llm.dto.ProviderDTO
import cn.cotenite.domain.llm.model.ProviderAggregate
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import cn.cotenite.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.interfaces.dto.llm.ProviderUpdateRequest
import java.time.LocalDateTime

/**
 * 服务提供商对象转换器
 */
object ProviderAssembler {

    fun toDTO(provider: ProviderEntity?): ProviderDTO? = provider?.let {
        ProviderDTO(
            id = it.id,
            protocol = it.protocol,
            name = it.name,
            description = it.description,
            config = it.config,
            isOfficial = it.isOfficial,
            status = it.status,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt,
            models = mutableListOf()
        ).apply { maskSensitiveInfo() }
    }

    fun toDTO(provider: ProviderAggregate?): ProviderDTO? = provider?.let {
        toDTO(it.entity)?.apply {
            it.getModels().mapNotNull(ModelAssembler::toDTO).forEach { modelDTO ->
                modelDTO.isOfficial = it.getIsOfficial()
                models.add(modelDTO)
            }
        }
    }

    fun toEntity(request: ProviderCreateRequest, userId: String): ProviderEntity {
        val now = LocalDateTime.now()
        return ProviderEntity().apply {
            this.userId = userId
            protocol = request.protocol
            name = request.name
            description = request.description
            config = request.config
            status = request.status ?: true
            createdAt = now
            updatedAt = now
        }
    }

    fun toEntity(request: ProviderUpdateRequest, userId: String): ProviderEntity = ProviderEntity().apply {
        id = request.id
        this.userId = userId
        protocol = request.protocol
        name = request.name
        description = request.description
        config = request.config
        status = request.status ?: true
        updatedAt = LocalDateTime.now()
    }

    fun toDTO(provider: Map<String, Any?>?): ProviderDTO? = provider?.let {
        val protocol = when (val rawProtocol = it["protocol"]) {
            is ProviderProtocol -> rawProtocol
            is String -> ProviderProtocol.entries.firstOrNull { protocol -> protocol.name == rawProtocol }
            else -> null
        }
        val config = when (val rawConfig = it["config"]) {
            is ProviderConfig -> rawConfig
            is Map<*, *> -> ProviderConfig(
                apiKey = rawConfig["apiKey"] as? String,
                baseUrl = rawConfig["baseUrl"] as? String,
                extras = rawConfig.filterKeys { key -> key is String }.mapKeys { entry -> entry.key as String }.toMutableMap()
            )
            else -> null
        }

        ProviderDTO(
            id = it["id"] as? String,
            protocol = protocol,
            name = it["name"] as? String,
            description = it["description"] as? String,
            config = config,
            isOfficial = it["isOfficial"] as? Boolean,
            status = it["status"] as? Boolean,
            createdAt = it["createdAt"] as? LocalDateTime,
            updatedAt = it["updatedAt"] as? LocalDateTime,
            models = mutableListOf()
        ).apply { maskSensitiveInfo() }
    }
}
