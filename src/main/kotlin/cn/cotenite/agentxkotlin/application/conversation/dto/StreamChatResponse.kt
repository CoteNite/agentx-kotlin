package cn.cotenite.agentxkotlin.application.conversation.dto

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:48
 */
data class StreamChatResponse(
    val content: String,
    val done: Boolean,
    val sessionId: String,
    val provider: String,
    val model: String,
    val timestamp : Long=System.currentTimeMillis(),
)
