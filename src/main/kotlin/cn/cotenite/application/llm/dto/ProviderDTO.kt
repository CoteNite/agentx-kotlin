package cn.cotenite.application.llm.dto

import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import java.time.LocalDateTime

/**
 * 服务提供商DTO
 */
data class ProviderDTO(
    var id: String? = null,
    var protocol: ProviderProtocol? = null,
    var name: String? = null,
    var description: String? = null,
    var config: ProviderConfig? = null,
    var isOfficial: Boolean? = null,
    var status: Boolean? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var models: MutableList<ModelDTO> = mutableListOf()
) {
    /**
     * 脱敏配置信息（用于返回前端）
     */
    fun maskSensitiveInfo() {
        config?.apiKey
            ?.takeIf { it.isNotBlank() }
            ?.let { config?.apiKey = "***********" }
    }
}
