package cn.cotenite.application.conversation.dto

/**
 * 流式聊天请求DTO
 */
data class StreamChatRequest(
    var message: String? = null,
    var sessionId: String? = null,
    var stream: Boolean = true
)
