package cn.cotenite.agentxkotlin.domain.token.service.factory

import cn.cotenite.agentxkotlin.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.agentxkotlin.domain.token.model.enums.TokenOverflowStrategyEnum
import cn.cotenite.agentxkotlin.domain.token.model.enums.TokenOverflowStrategyEnum.*
import cn.cotenite.agentxkotlin.domain.token.service.TokenOverflowStrategy
import cn.cotenite.agentxkotlin.domain.token.service.factory.TokenOverflowStrategyFactory.createStrategy
import cn.cotenite.agentxkotlin.domain.token.service.impl.NoTokenOverflowStrategy
import cn.cotenite.agentxkotlin.domain.token.service.impl.SlidingWindowTokenOverflowStrategy
import cn.cotenite.agentxkotlin.domain.token.service.impl.SummarizeTokenOverflowStrategy
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 00:10
 */
@Service
object TokenOverflowStrategyFactory{

    fun createStrategy(strategyType: TokenOverflowStrategyEnum?, config: TokenOverflowConfig): TokenOverflowStrategy {
        if (strategyType == null) {
            return NoTokenOverflowStrategy()
        }
        return when (config.strategyType) {
            NONE -> NoTokenOverflowStrategy(config)
            SLIDING_WINDOW -> SlidingWindowTokenOverflowStrategy(config)
            SUMMARIZE -> SummarizeTokenOverflowStrategy(config)
        }
    }

    /**
     * 根据策略名称字符串创建对应的策略实例
     *
     * @param strategyName 策略名称字符串
     * @param config 策略配置
     * @return 策略实例
     */
    fun createStrategy(strategyName: String, config: TokenOverflowConfig): TokenOverflowStrategy {
        val strategyType = TokenOverflowStrategyEnum.fromString(strategyName)
        return createStrategy(strategyType, config)
    }

    /**
     * 根据配置创建对应的策略实例
     *
     * @param config 策略配置
     * @return 策略实例
     */
    fun createStrategy(config: TokenOverflowConfig?): TokenOverflowStrategy {
        return config?.let { createStrategy(it.strategyType, it) } ?: NoTokenOverflowStrategy()
    }

}