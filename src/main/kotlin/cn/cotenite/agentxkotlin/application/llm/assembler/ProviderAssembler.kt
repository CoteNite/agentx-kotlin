package cn.cotenite.agentxkotlin.application.llm.assembler

import cn.cotenite.agentxkotlin.application.llm.dto.ProviderDTO
import cn.cotenite.agentxkotlin.domain.llm.model.ProviderAggregate
import cn.cotenite.agentxkotlin.domain.llm.model.ProviderEntity
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderUpdateRequest
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:10
 */
object ProviderAssembler {


    /**
     * 將實體轉換為 DTO，並進行敏感資訊脫敏
     */
    fun toDTO(provider: ProviderEntity): ProviderDTO {
        // 使用安全調用 . 和 let 函數，簡潔地處理可能為 null 的 ProviderEntity
        return provider.let {
            val dto = ProviderDTO(
                id = it.id,
                protocol = it.protocol,
                name = it.name,
                description = it.description,
                config = it.config,
                isOfficial = it.isOfficial,
                status = it.status,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
            dto.maskSensitiveInfo()
            dto
        }
    }

    /**
     * 將多個聚合根轉換為 DTO 列表
     */
    fun toDTOList(providers: List<ProviderAggregate>): List<ProviderDTO> {
        return providers.map { toDTO(it) }
    }

    /**
     * 將多個實體轉換為 DTO 列表
     */
    fun toDTOListFromEntities(providers: List<ProviderEntity>): List<ProviderDTO> {
        // Kotlin 慣用法：如果列表為 null 或為空，則返回空列表
        return providers.map { toDTO(it) }
    }

    /**
     * 將創建請求轉換為實體
     */
    fun toEntity(request: ProviderCreateRequest, userId: String): ProviderEntity {
        val now = LocalDateTime.now()
        // 使用 ProviderEntity 的主構造函數創建實例
        val providerEntity = ProviderEntity(
            userId = userId,
            protocol = request.protocol,
            name = request.name,
            description = request.description,
            config = request.config, // 會自動加密
            status = request.status,
        )
        providerEntity.createdAt = now
        providerEntity.updatedAt = now
        return providerEntity
    }

    /**
     * 將更新請求轉換為實體
     */
    fun toEntity(request: ProviderUpdateRequest, userId: String): ProviderEntity {
        val now = LocalDateTime.now()
        val providerEntity = ProviderEntity(
            id = request.id,
            userId = userId,
            protocol = request.protocol,
            name = request.name,
            description = request.description,
            config = request.config,
            status = request.status?:throw BusinessException("status is required"),
        )
        providerEntity.updatedAt=now
        return providerEntity
    }

    /**
     * 根據更新請求更新實體
     */
    fun updateEntity(entity: ProviderEntity, request: ProviderUpdateRequest) {
        entity.let { targetEntity ->
            request.let { updateRequest ->
                updateRequest.name.let { targetEntity.name = it }
                updateRequest.description.let { targetEntity.description = it }
                updateRequest.config.let { targetEntity.config = it } // 會自動加密
                updateRequest.status.let {
                    if (it != null) {
                        targetEntity.status = it
                    }
                }
                targetEntity.updatedAt = LocalDateTime.now()
            }
        }
    }

    /**
     * 將聚合根轉換為 DTO
     * 包含 ModelDTO 列表
     */
    fun toDTO(providerAggregate: ProviderAggregate): ProviderDTO {
        return providerAggregate.let { aggregate ->
            val dto = toDTO(aggregate.entity)
            aggregate.models.let { models ->
                if (models.isNotEmpty()) {
                    // 使用 mapNotNull 將 ModelEntity 列表轉換為 ModelDTO 列表
                    val modelDTOs = models.map { modelEntity ->
                        // 調用 ModelAssembler 的 toDTO 方法
                        val modelDTO = ModelAssembler.toDTO(modelEntity)
                        // 為每個 ModelDTO 設置 isOfficial 屬性
                        modelDTO.isOfficial = aggregate.isOfficial // 對應 Java 的 getIsOfficial()
                        modelDTO
                    }.toMutableList() // 轉換為可變列表以賦值給 ProviderDTO
                    dto.models = modelDTOs
                }
            }
            dto
        }
    }

}