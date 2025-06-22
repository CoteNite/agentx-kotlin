package cn.cotenite.agentxkotlin.application.conversation.dto

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:48
 */
data class StreamChatResponse(
    val content: String,
    val done: Boolean,
    val sessionId: String?=null,
    val provider: String?=null,
    val model: String?=null,
    val timestamp : Long=System.currentTimeMillis(),
)
