package cn.cotenite.domain.token.service

import org.springframework.stereotype.Service
import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.impl.NoTokenOverflowStrategy
import cn.cotenite.domain.token.service.impl.SlidingWindowTokenOverflowStrategy
import cn.cotenite.domain.token.service.impl.SummarizeTokenOverflowStrategy

/**
 * Token策略工厂
 */
@Service
class TokenOverflowStrategyFactory {

    fun createStrategy(config: TokenOverflowConfig?): TokenOverflowStrategy =
        config?.let { createStrategy(it.strategyType, it) } ?: NoTokenOverflowStrategy()

    fun createStrategy(strategyType: TokenOverflowStrategyEnum?, config: TokenOverflowConfig): TokenOverflowStrategy =
        when (strategyType ?: TokenOverflowStrategyEnum.NONE) {
            TokenOverflowStrategyEnum.SLIDING_WINDOW -> SlidingWindowTokenOverflowStrategy(config)
            TokenOverflowStrategyEnum.SUMMARIZE -> SummarizeTokenOverflowStrategy(config)
            TokenOverflowStrategyEnum.NONE -> NoTokenOverflowStrategy(config)
        }

    fun createStrategy(strategyName: String?, config: TokenOverflowConfig): TokenOverflowStrategy =
        createStrategy(TokenOverflowStrategyEnum.fromString(strategyName), config)
}
