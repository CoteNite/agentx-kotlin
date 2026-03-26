package cn.cotenite.domain.token.model

/**
 * Token处理结果
 */
data class TokenProcessResult(
    var retainedMessages: List<TokenMessage> = emptyList(),
    var summary: String? = null,
    var totalTokens: Int = 0,
    var strategyName: String = "NONE",
    var processed: Boolean = false
)
