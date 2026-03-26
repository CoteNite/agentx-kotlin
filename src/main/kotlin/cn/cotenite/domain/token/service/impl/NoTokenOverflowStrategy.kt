package cn.cotenite.domain.token.service.impl

import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.TokenProcessResult
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.TokenOverflowStrategy

/**
 * 无策略实现
 */
class NoTokenOverflowStrategy(
    private val config: TokenOverflowConfig = TokenOverflowConfig.createDefault()
) : TokenOverflowStrategy {

    override fun process(messages: List<TokenMessage>, tokenOverflowConfig: TokenOverflowConfig): TokenProcessResult =
        TokenProcessResult(
            retainedMessages = messages,
            strategyName = getName(),
            processed = false,
            totalTokens = calculateTotalTokens(messages)
        )

    override fun getName(): String = TokenOverflowStrategyEnum.NONE.name

    override fun needsProcessing(messages: List<TokenMessage>): Boolean = false

    private fun calculateTotalTokens(messages: List<TokenMessage>): Int =
        messages.sumOf { it.tokenCount ?: 0 }
}
