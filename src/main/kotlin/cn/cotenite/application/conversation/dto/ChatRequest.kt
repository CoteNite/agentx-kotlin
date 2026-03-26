package cn.cotenite.application.conversation.dto

import jakarta.validation.constraints.NotBlank

/**
 * 聊天请求DTO
 */
data class ChatRequest(
    @field:NotBlank(message = "消息内容不可为空")
    var message: String? = null,
    @field:NotBlank(message = "会话id不可为空")
    var sessionId: String? = null
)
