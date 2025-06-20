package cn.cotenite.agentxkotlin.domain.token.model

import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/20 22:36
 */
data class TokenMessage(
    val id: String,
    val content: String,
    val role: String,
    val tokenCount: Int,
    val createdAt: LocalDateTime=LocalDateTime.now(),
)