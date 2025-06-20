package cn.cotenite.agentxkotlin.domain.token.model.config

import cn.cotenite.agentxkotlin.domain.token.model.enums.TokenOverflowStrategyEnum
import org.springframework.stereotype.Service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 02:19
 */
@Service
data class TokenOverflowConfig(
    /**
     * 策略类型
     */
    var strategyType: TokenOverflowStrategyEnum = TokenOverflowStrategyEnum.NONE,

    /**
     * 最大Token数，适用于滑动窗口和摘要策略
     */
    var maxTokens: Int? = null,

    /**
     * 预留缓冲比例，适用于滑动窗口策略
     * 范围0-1之间的小数，表示预留的空间比例
     */
    var reserveRatio: Double? = null,

    /**
     * 摘要触发阈值（消息数量），适用于摘要策略
     */
    var summaryThreshold: Int? = null
) {
    /**
     * 创建默认的无策略配置
     *
     * @return 无策略配置实例
     */
    companion object {
        @JvmStatic // 允许在 Java 代码中像静态方法一样调用
        fun createDefault(): TokenOverflowConfig {
            return TokenOverflowConfig(strategyType = TokenOverflowStrategyEnum.NONE)
        }

        /**
         * 创建滑动窗口策略配置
         *
         * @param maxTokens 最大Token数
         * @param reserveRatio 预留缓冲比例，默认0.1
         * @return 滑动窗口策略配置实例
         */
        @JvmStatic
        fun createSlidingWindowConfig(maxTokens: Int, reserveRatio: Double? = 0.1): TokenOverflowConfig {
            return TokenOverflowConfig(
                strategyType = TokenOverflowStrategyEnum.SLIDING_WINDOW,
                maxTokens = maxTokens,
                reserveRatio = reserveRatio
            )
        }

        /**
         * 创建摘要策略配置
         *
         * @param maxTokens 最大Token数
         * @param summaryThreshold 摘要触发阈值，默认20
         * @return 摘要策略配置实例
         */
        @JvmStatic
        fun createSummaryConfig(maxTokens: Int, summaryThreshold: Int? = 20): TokenOverflowConfig {
            return TokenOverflowConfig(
                strategyType = TokenOverflowStrategyEnum.SUMMARIZE,
                maxTokens = maxTokens,
                summaryThreshold = summaryThreshold
            )
        }
    }
}