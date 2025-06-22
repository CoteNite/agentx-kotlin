package cn.cotenite.agentxkotlin.domain.token.service.impl

import cn.cotenite.agentxkotlin.domain.token.model.TokenMessage
import cn.cotenite.agentxkotlin.domain.token.model.TokenProcessResult
import cn.cotenite.agentxkotlin.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.agentxkotlin.domain.sahred.enums.TokenOverflowStrategyEnum
import cn.cotenite.agentxkotlin.domain.token.service.TokenOverflowStrategy
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import org.springframework.stereotype.Service
import kotlin.collections.MutableList
import kotlin.collections.sumOf


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 01:50
 */
@Service
class SlidingWindowTokenOverflowStrategy(
    private val config: TokenOverflowConfig,
):TokenOverflowStrategy {

    companion object{
        /**
         * 默认最大Token数
         */
        private const val DEFAULT_MAX_TOKENS: Int = 4096

        /**
         * 默认预留缓冲比例
         */
        private const val DEFAULT_RESERVE_RATIO: Double = 0.1
    }

    /**
     * 处理消息列表，应用滑动窗口策略
     *
     * @param messages 待处理的消息列表
     * @return 处理后保留的消息列表
     */
    override fun process(messages: MutableList<TokenMessage>): TokenProcessResult {
        if (!this.needsProcessing(messages)) {
            return TokenProcessResult(
                retainedMessages = messages,
                totalTokens = this.calculateTotalTokens(messages),
                strategyName = this.getName(),
                processed = false
            )
        }

        val sortedMessages = messages.sortedBy { it.createdAt }.reversed()

        val maxTokens = config.maxTokens ?: throw BusinessException("maxTokens is null")
        val reserveRatio = config.reserveRatio ?: throw BusinessException("reserveRatio is null")

        val reserveTokens = (maxTokens * reserveRatio).toInt()
        val availableTokens = maxTokens - reserveTokens

        val retainedMessages=mutableListOf<TokenMessage>()
        var totalTokens = 0

        for (message in sortedMessages) {
            val messageTokens = message.tokenCount
            if (totalTokens + messageTokens <= availableTokens) {
                retainedMessages.add(message)
                totalTokens += messageTokens
            } else {
                break
            }
        }

        return TokenProcessResult(
            retainedMessages = retainedMessages,
            totalTokens = totalTokens,
            strategyName = this.getName(),
            processed = true
        )
    }

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    override fun getName(): String {
        return TokenOverflowStrategyEnum.SLIDING_WINDOW.name
    }

    /**
     * 判断是否需要进行Token超限处理
     *
     * @param messages 待处理的消息列表
     * @return 是否需要处理
     */
    override fun needsProcessing(messages: MutableList<TokenMessage>): Boolean {
        if (messages.isEmpty()) {
            return false
        }

        val totalTokens = calculateTotalTokens(messages)
        val maxTokens= config.maxTokens?:throw BusinessException("maxTokens is null")
        return totalTokens > maxTokens
    }

    /**
     * 计算消息列表的总token数
     */
    private fun calculateTotalTokens(messages: MutableList<TokenMessage>): Int {
        return messages.sumOf { m -> m.tokenCount }
    }

    /**
     * 获取配置的最大Token数，如果未配置则使用默认值
     *
     * @return 最大Token数
     */
    private fun getMaxTokens(): Int {
        return config.maxTokens ?: DEFAULT_MAX_TOKENS
    }

    /**
     * 获取配置的预留比例，如果未配置则使用默认值
     *
     * @return 预留比例
     */
    private fun getReserveRatio(): Double {
        return config.reserveRatio?: DEFAULT_RESERVE_RATIO
    }
}