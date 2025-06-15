package cn.cotenite.agentxkotlin.application.conversation.dto

import jakarta.validation.constraints.NotBlank

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:41
 */
open class ChatRequest(
    @NotBlank(message = "消息内容不能为空")
    open val message: String,
    open var provider: String,
    open val model: String="",
    open val sessionId: String
)
