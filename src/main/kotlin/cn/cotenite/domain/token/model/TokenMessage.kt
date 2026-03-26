package cn.cotenite.domain.token.model

import java.time.LocalDateTime

/**
 * Token消息模型
 */
data class TokenMessage(
    var id: String? = null,
    var content: String? = null,
    var role: String? = null,
    var tokenCount: Int? = null,
    var createdAt: LocalDateTime = LocalDateTime.now()
)
