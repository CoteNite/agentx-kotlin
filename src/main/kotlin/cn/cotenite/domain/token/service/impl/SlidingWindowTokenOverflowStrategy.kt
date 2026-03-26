package cn.cotenite.domain.token.service.impl

import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.TokenProcessResult
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.TokenOverflowStrategy

/**
 * 滑动窗口策略
 */
class SlidingWindowTokenOverflowStrategy(
    private val config: TokenOverflowConfig
) : TokenOverflowStrategy {

    override fun process(messages: List<TokenMessage>, tokenOverflowConfig: TokenOverflowConfig): TokenProcessResult {
        if (!needsProcessing(messages)) {
            return TokenProcessResult(
                retainedMessages = messages,
                strategyName = getName(),
                processed = false,
                totalTokens = calculateTotalTokens(messages)
            )
        }

        val maxTokens = config.maxTokens ?: 4096
        var totalTokens = 0
        val retained = buildList {
            for (message in messages.sortedByDescending(TokenMessage::createdAt)) {
                val tokens = message.tokenCount ?: 0
                if (totalTokens + tokens > maxTokens) break
                add(message)
                totalTokens += tokens
            }
        }

        return TokenProcessResult(
            retainedMessages = retained,
            strategyName = getName(),
            processed = true,
            totalTokens = totalTokens
        )
    }

    override fun getName(): String = TokenOverflowStrategyEnum.SLIDING_WINDOW.name

    override fun needsProcessing(messages: List<TokenMessage>): Boolean =
        calculateTotalTokens(messages) > (config.maxTokens ?: 4096)

    private fun calculateTotalTokens(messages: List<TokenMessage>): Int =
        messages.sumOf { it.tokenCount ?: 0 }
}
