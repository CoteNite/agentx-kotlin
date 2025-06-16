package cn.cotenite.agentxkotlin.domain.conversation.model

import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:17
 */
data class MessageDTO(
    val id:String,
    val role:String,
    val content:String,
    val createdAt: LocalDateTime,
    val provider:String,
    val model:String
)
