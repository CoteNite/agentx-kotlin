package cn.cotenite.agentxkotlin.domain.token.service.impl

import cn.cotenite.agentxkotlin.domain.token.model.TokenMessage
import cn.cotenite.agentxkotlin.domain.token.model.TokenProcessResult
import cn.cotenite.agentxkotlin.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.agentxkotlin.domain.token.model.enums.TokenOverflowStrategyEnum
import cn.cotenite.agentxkotlin.domain.token.service.TokenOverflowStrategy
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import org.springframework.stereotype.Service
import java.util.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 00:15
 */
@Service
class SummarizeTokenOverflowStrategy(
    private val config: TokenOverflowConfig,
    val messageToSummarize: MutableList<TokenMessage> =mutableListOf(),
    var summaryMessage: TokenMessage?=null
):TokenOverflowStrategy{

    companion object {

        /**
         * 默认摘要触发阈值（消息数量）
         */
        const val DEFAULT_SUMMARY_THRESHOLD: Int = 20

        /**
         * 默认最大Token数
         */
        const val DEFAULT_MAX_TOKENS: Int = 4096

        /**
         * 摘要消息的特殊角色标识
         */
        const val SUMMARY_ROLE: String = "summary"
    }

    override fun process(messages: MutableList<TokenMessage>): TokenProcessResult {
        if (!this.needsProcessing(messages)){
            return TokenProcessResult(
                retainedMessages = messages,
                totalTokens = this.calculateTotalTokens(messages),
                strategyName = this.getName(),
                processed = false
            )
        }

        val sortedMessages = messages.sortedBy { it.createdAt }

        val threshold  = config.summaryThreshold?:throw BusinessException("Invalid summaryThreshold")

        val messagesToSummarize = sortedMessages.subList(0, sortedMessages.size - threshold)
        val retainedMessages = sortedMessages.subList(sortedMessages.size - threshold, sortedMessages.size).toMutableList()


        val summary = this.generateSummary(messagesToSummarize)
        summaryMessage = this.createSummaryMessage(summary)
        summaryMessage?.let { retainedMessages.add(0, it) }

        return TokenProcessResult(
            retainedMessages = retainedMessages,
            summary=summary,
            totalTokens = this.calculateTotalTokens(retainedMessages),
            strategyName = this.getName(),
            processed = true
        )
    }

    private fun calculateTotalTokens(messages: List<TokenMessage>): Int {
        return messages.sumOf { it.tokenCount }
    }

    private fun generateSummary(messages: List<TokenMessage>): String {
        // TODO: 这里应该调用LLM生成摘要，目前返回简单描述
        return String.format("这里是%d条历史消息的摘要", messages.size)
    }

    /**
     * 创建表示摘要的TokenMessage对象
     *
     * @param summary 摘要内容
     * @return 摘要消息对象
     */
    private fun createSummaryMessage(summary: String): TokenMessage {
        return TokenMessage(
            id = UUID.randomUUID().toString(),
            content = SUMMARY_ROLE,
            role = summary,
            tokenCount = 100,
        )
    }

    override fun getName(): String {
        return TokenOverflowStrategyEnum.SUMMARIZE.name
    }

    override fun needsProcessing(messages: MutableList<TokenMessage>): Boolean {
        return config.summaryThreshold?.let { threshold -> messages.size > threshold } ?: false
    }
}