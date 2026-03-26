package cn.cotenite.domain.token.model.config

import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.infrastructure.llm.config.ProviderConfig

/**
 * Token超限配置
 */
data class TokenOverflowConfig(
    var strategyType: TokenOverflowStrategyEnum = TokenOverflowStrategyEnum.NONE,
    var maxTokens: Int? = null,
    var reserveRatio: Double = 0.3,
    var summaryThreshold: Int? = null,
    var providerConfig: ProviderConfig? = null
) {
    companion object {
        fun createDefault(): TokenOverflowConfig = TokenOverflowConfig(TokenOverflowStrategyEnum.NONE)

        fun createSlidingWindowConfig(maxTokens: Int, reserveRatio: Double?): TokenOverflowConfig =
            TokenOverflowConfig(
                strategyType = TokenOverflowStrategyEnum.SLIDING_WINDOW,
                maxTokens = maxTokens,
                reserveRatio = reserveRatio ?: 0.1
            )

        fun createSummaryConfig(maxTokens: Int, summaryThreshold: Int?): TokenOverflowConfig =
            TokenOverflowConfig(
                strategyType = TokenOverflowStrategyEnum.SUMMARIZE,
                maxTokens = maxTokens,
                summaryThreshold = summaryThreshold ?: 20
            )
    }
}
