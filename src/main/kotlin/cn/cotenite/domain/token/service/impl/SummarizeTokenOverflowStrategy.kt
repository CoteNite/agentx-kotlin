package cn.cotenite.domain.token.service.impl

import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.TokenProcessResult
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.TokenOverflowStrategy
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.LLMProviderService
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import java.time.LocalDateTime
import java.util.UUID

/**
 * 摘要策略 Token 超限处理实现
 * 将超出阈值的早期消息生成摘要，保留摘要和最新消息
 */
class SummarizeTokenOverflowStrategy(private val config: TokenOverflowConfig) : TokenOverflowStrategy {

    /** 摘要消息的特殊角色标识 */
    companion object {
        private const val SUMMARY_ROLE = "summary"
    }

    /** 需要进行摘要的消息（只读暴露给外部） */
    var messagesToSummarize: List<TokenMessage> = emptyList()
        private set

    /** 生成的摘要消息对象 */
    var summaryMessage: TokenMessage? = null
        private set

    /**
     * 处理消息列表，应用摘要策略
     */
    override fun process(messages: List<TokenMessage>, tokenOverflowConfig: TokenOverflowConfig): TokenProcessResult {
        if (!needsProcessing(messages)) {
            return TokenProcessResult().apply {
                retainedMessages = messages
                strategyName = getName()
                processed = false
                totalTokens = calculateTotalTokens(messages)
            }
        }

        // 按时间排序并根据阈值分割
        val sortedMessages = messages.sortedBy { it.createdAt }
        val threshold = config.summaryThreshold?:throw BusinessException("未设置token阈值")

        // 分割：旧消息去摘要，新消息保留
        val splitIndex = sortedMessages.size - threshold
        messagesToSummarize = sortedMessages.subList(0, splitIndex)
        val retainedMessages = sortedMessages.subList(splitIndex, sortedMessages.size).toMutableList()

        // 生成摘要并创建消息对象
        val summaryContent = generateSummary(messagesToSummarize, tokenOverflowConfig)
        val summaryMsg = createSummaryMessage(summaryContent)
        this.summaryMessage = summaryMsg

        // 将摘要消息添加到开头
        retainedMessages.add(0, summaryMsg)

        return TokenProcessResult().apply {
            this.retainedMessages = retainedMessages
            this.summary = summaryContent
            this.strategyName = getName()
            this.processed = true
            this.totalTokens = calculateTotalTokens(retainedMessages)
        }
    }

    private fun createSummaryMessage(summary: String): TokenMessage {
        return TokenMessage().apply {
            id = UUID.randomUUID().toString()
            role = SUMMARY_ROLE
            content = summary
            tokenCount = 100 // TODO: 计算实际 token 数
            createdAt = LocalDateTime.now()
        }
    }

    override fun getName(): String = TokenOverflowStrategyEnum.SUMMARIZE.name

    override fun needsProcessing(messages: List<TokenMessage>): Boolean {
        if (messages.isEmpty()) return false
        return messages.size > config.summaryThreshold!!
    }

    /**
     * 调用大模型生成摘要
     */
    private fun generateSummary(messages: List<TokenMessage>, tokenOverflowConfig: TokenOverflowConfig): String {
        val providerConfig = tokenOverflowConfig.providerConfig
        val chatModel = LLMProviderService.getStrand(providerConfig!!.protocol, providerConfig)

        val systemMessage = SystemMessage(
            """
            你是一个专业的对话摘要生成器，请严格按照以下要求工作：
            1. 只基于提供的对话内容生成客观摘要，不得添加任何原对话中没有的信息
            2. 特别关注：用户问题、回答中的关键信息、重要事实
            3. 去除所有寒暄、表情符号和情感表达
            4. 使用简洁的第三人称陈述句
            5. 保持时间顺序和逻辑关系
            6. 示例格式：[用户]问... [AI]回答...
            禁止使用任何表情符号或拟人化表达
        """.trimIndent()
        )

        val userMessage = UserMessage(messages.map { TextContent(it.content) })

        val chatResponse = chatModel.chat(listOf(systemMessage, userMessage))
        return chatResponse.aiMessage().text()
    }

    private fun calculateTotalTokens(messages: List<TokenMessage>): Int {
        return messages.sumOf { it.tokenCount ?: 0 }
    }
}