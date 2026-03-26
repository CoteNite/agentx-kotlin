package cn.cotenite.application.conversation.dto

/**
 * 聊天响应DTO
 */
data class ChatResponse(
    var content: String? = null,
    var sessionId: String? = null,
    var provider: String? = null,
    var model: String? = null,
    var timestamp: Long = System.currentTimeMillis()
)
