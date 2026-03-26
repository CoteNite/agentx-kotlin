package cn.cotenite.domain.token.model

/**
 * Token结果模型
 */
data class TokenResult(
    var retainedMessages: List<TokenMessage> = emptyList(),
    var summary: String? = null,
    var strategyName: String? = null,
    var totalTokens: Int = 0
)
