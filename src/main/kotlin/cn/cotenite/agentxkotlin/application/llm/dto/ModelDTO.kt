package cn.cotenite.agentxkotlin.application.llm.dto

import cn.cotenite.agentxkotlin.domain.llm.model.config.LLMModelConfig
import cn.cotenite.agentxkotlin.domain.llm.model.enums.ModelType
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:06
 */
data class ModelDTO(
    /**
     * 模型ID
     */
    var id: String? = null, // 使用可空類型 String? 並提供默認值 null

    /**
     * 用戶ID
     */
    var userId: String? = null,

    /**
     * 服務商ID
     */
    var providerId: String? = null,

    /**
     * 服務商名稱 (額外添加，便於前端顯示)
     */
    var providerName: String? = null,

    /**
     * 模型ID
     */
    var modelId: String? = null,

    /**
     * 模型名稱
     */
    var name: String? = null,

    /**
     * 模型描述
     */
    var description: String? = null,

    /**
     * 模型類型
     */
    var type: ModelType? = null,

    /**
     * 模型配置
     */
    var config: LLMModelConfig? = null,

    /**
     * 是否官方
     */
    var isOfficial: Boolean? = null, // 直接使用 isOfficial 作為屬性名，對應 Java 的 getIsOfficial()

    /**
     * 模型狀態
     */
    var status: Boolean? = null,

    /**
     * 創建時間
     */
    var createdAt: LocalDateTime? = null,

    /**
     * 更新時間
     */
    var updatedAt: LocalDateTime? = null
)