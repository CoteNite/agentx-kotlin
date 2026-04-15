package cn.cotenite.application.conversation.dto

import java.time.LocalDateTime

/**
 * 会话DTO
 */
data class SessionDTO(
    var id: String? = null,
    var title: String? = null,
    var description: String? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var isArchived: Boolean = false,
    var agentId: String? = null,
    var multiModel: Boolean?=null
)
