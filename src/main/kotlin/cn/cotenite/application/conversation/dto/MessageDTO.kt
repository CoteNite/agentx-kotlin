package cn.cotenite.application.conversation.dto

import cn.cotenite.domain.conversation.constant.Role
import java.time.LocalDateTime

/**
 * 消息DTO
 */
data class MessageDTO(
    var id: String? = null,
    var role: Role? = null,
    var content: String? = null,
    var createdAt: LocalDateTime? = null,
    var provider: String? = null,
    var model: String? = null
)
