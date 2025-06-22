package cn.cotenite.agentxkotlin.application.llm.assembler

import cn.cotenite.agentxkotlin.application.llm.dto.ModelDTO
import cn.cotenite.agentxkotlin.domain.llm.model.ModelEntity
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelUpdateRequest
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:05
 */
object ModelAssembler {


    /**
     * 將領域物件轉換為 DTO
     */
    fun toDTO(model: ModelEntity): ModelDTO {
        // 使用安全調用 . 和 let 函數，簡潔地處理可能為 null 的 ModelEntity
        return model.let {
            ModelDTO(
                id = it.id,
                userId = it.userId,
                providerId = it.providerId,
                modelId = it.modelId,
                name = it.name,
                description = it.description,
                type = it.type,
                config = it.config,
                status = it.status,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                isOfficial = it.isOfficial // 自動轉換 Java 的 getOfficial() 為 Kotlin 屬性 isOfficial
            )
        }
    }

    /**
     * 將多個領域物件轉換為 DTO 列表
     */
    fun toDTOs(models: List<ModelEntity>): List<ModelDTO> {
        return models.map { toDTO(it) }
    }

    /**
     * 將創建請求轉換為領域物件
     */
    fun toEntity(request: ModelCreateRequest, userId: String): ModelEntity {
        val now = LocalDateTime.now()
        // 使用 ModelEntity 的主構造函數創建實例
        val modelEntity = ModelEntity(
            userId = userId,
            providerId = request.providerId,
            modelId = request.modelId,
            name = request.name,
            description = request.description,
            type = request.type,
            config = request.config,
            )
        modelEntity.createdAt = now
        modelEntity.updatedAt = now
        return modelEntity
    }

    /**
     * 將更新請求轉換為領域物件
     */
    fun toEntity(request: ModelUpdateRequest, userId: String): ModelEntity {
        val modelEntity = ModelEntity(
            id = request.id, // 更新時通常會帶上 ID
            userId = userId, // 確保 userId 也被傳遞
            name = request.name,
            description = request.description,
            modelId = request.modelId,
            config = request.config,
        )
        modelEntity.updatedAt=LocalDateTime.now()
        return modelEntity
    }
}