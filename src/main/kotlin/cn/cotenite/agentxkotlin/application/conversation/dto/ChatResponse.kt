package cn.cotenite.agentxkotlin.application.conversation.dto

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:42
 */
data class ChatResponse(
    val content: String,
    val provider: String,
    val model: String,
    val sessionId: String,
    val timestamp : Long=System.currentTimeMillis()
)
