package cn.cotenite.domain.token.service.impl

import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.TokenProcessResult
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.TokenOverflowStrategy
import java.time.LocalDateTime
import java.util.UUID

/**
 * 摘要策略
 */
class SummarizeTokenOverflowStrategy(
    private val config: TokenOverflowConfig
) : TokenOverflowStrategy {

    private var messagesToSummarize: List<TokenMessage> = emptyList()
    private var summaryMessage: TokenMessage? = null

    override fun process(messages: List<TokenMessage>, tokenOverflowConfig: TokenOverflowConfig): TokenProcessResult {
        if (!needsProcessing(messages)) {
            return TokenProcessResult(
                retainedMessages = messages,
                strategyName = getName(),
                processed = false,
                totalTokens = calculateTotalTokens(messages)
            )
        }

        val threshold = config.summaryThreshold ?: 20
        val sortedMessages = messages.sortedBy(TokenMessage::createdAt)
        val splitIndex = (sortedMessages.size - threshold).coerceAtLeast(1)

        messagesToSummarize = sortedMessages.take(splitIndex)
        val summary = generateSummary(messagesToSummarize)

        val generatedSummaryMessage = TokenMessage(
            id = UUID.randomUUID().toString(),
            role = "summary",
            content = summary,
            tokenCount = 100,
            createdAt = LocalDateTime.now()
        )
        summaryMessage = generatedSummaryMessage

        val retainedMessages = buildList {
            add(generatedSummaryMessage)
            addAll(sortedMessages.drop(splitIndex))
        }

        return TokenProcessResult(
            retainedMessages = retainedMessages,
            summary = summary,
            strategyName = getName(),
            processed = true,
            totalTokens = calculateTotalTokens(retainedMessages)
        )
    }

    override fun getName(): String = TokenOverflowStrategyEnum.SUMMARIZE.name

    override fun needsProcessing(messages: List<TokenMessage>): Boolean =
        messages.size > (config.summaryThreshold ?: 20)

    private fun generateSummary(messages: List<TokenMessage>): String =
        messages.joinToString("\n") { "[${it.role}] ${it.content.orEmpty()}" }

    private fun calculateTotalTokens(messages: List<TokenMessage>): Int =
        messages.sumOf { it.tokenCount ?: 0 }

    fun getMessagesToSummarize(): List<TokenMessage> = messagesToSummarize

    fun getSummaryMessage(): TokenMessage? = summaryMessage
}
