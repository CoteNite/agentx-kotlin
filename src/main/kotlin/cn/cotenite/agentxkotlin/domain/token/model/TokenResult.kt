package cn.cotenite.agentxkotlin.domain.token.model

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/20 22:37
 */
data class TokenResult(
    val retainedMessages:MutableList<TokenMessage>,
    val summary: String,
    val totalTokens: Int,
    val strategyName: String
)