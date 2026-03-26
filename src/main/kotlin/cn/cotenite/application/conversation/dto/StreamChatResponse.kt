package cn.cotenite.application.conversation.dto

/**
 * 流式聊天响应DTO
 */
data class StreamChatResponse(
    var content: String? = null,
    var done: Boolean = false,
    var sessionId: String? = null,
    var provider: String? = null,
    var model: String? = null,
    var timestamp: Long = System.currentTimeMillis()
)
