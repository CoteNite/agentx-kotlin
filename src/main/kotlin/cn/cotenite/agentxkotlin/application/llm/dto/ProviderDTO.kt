package cn.cotenite.agentxkotlin.application.llm.dto

import cn.cotenite.agentxkotlin.domain.llm.model.config.ProviderConfig
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:06
 */
data class ProviderDTO( // 將其聲明為 data class
    /**
     * 服務商ID
     */
    var id: String? = null, // 使用可空類型 String? 並提供默認值 null

    /**
     * 服務商協議
     */
    var protocol: ProviderProtocol? = null,

    /**
     * 服務商名稱
     */
    var name: String? = null,

    /**
     * 服務商描述
     */
    var description: String? = null,

    /**
     * 服務商配置
     */
    var config: ProviderConfig? = null,

    /**
     * 是否官方
     */
    var isOfficial: Boolean? = null,

    /**
     * 服務商狀態
     */
    var status: Boolean? = null,

    /**
     * 創建時間
     */
    var createdAt: LocalDateTime? = null,

    /**
     * 更新時間
     */
    var updatedAt: LocalDateTime? = null,

    /**
     * 模型列表
     * 提供默認值：空的MutableList
     */
    var models: MutableList<ModelDTO> = mutableListOf()
) {

    /**
     * 脫敏配置資訊（用於返回前端）
     */
    fun maskSensitiveInfo() {
        config?.let {
            if (it.apiKey.isNotEmpty()) {
                it.apiKey = "***********"
            }
        }
    }
}