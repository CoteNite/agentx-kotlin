package cn.cotenite.domain.conversation.service

import org.springframework.stereotype.Service
import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.TokenProcessResult
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.TokenDomainService
import cn.cotenite.infrastructure.llm.config.ProviderConfig

/**
 * 上下文处理器
 */
@Service
class ContextProcessor(
    private val contextDomainService: ContextDomainService,
    private val messageDomainService: MessageDomainService,
    private val tokenDomainService: TokenDomainService
) {

    fun processContext(
        sessionId: String,
        maxTokens: Int,
        strategyType: TokenOverflowStrategyEnum,
        summaryThreshold: Int,
        providerConfig: ProviderConfig
    ): ContextResult {
        val contextEntity = contextDomainService.findBySessionId(sessionId)
            ?: return ContextResult(ContextEntity().apply { this.sessionId = sessionId }, emptyList())

        val messageEntities = messageDomainService.listByIds(contextEntity.activeMessages)
        val tokenProcessResult = tokenDomainService.processMessages(
            tokenizeMessage(messageEntities),
            TokenOverflowConfig().apply {
                this.strategyType = strategyType
                this.maxTokens = maxTokens
                this.summaryThreshold = summaryThreshold
                this.providerConfig = providerConfig
            }
        )

        if (tokenProcessResult.processed) {
            contextEntity.activeMessages = tokenProcessResult.retainedMessages.map { it.id }.toMutableList()
            if (strategyType == TokenOverflowStrategyEnum.SUMMARIZE) {
                contextEntity.summary = (contextEntity.summary ?: "") + tokenProcessResult.summary.orEmpty()
            }
        }

        return ContextResult(contextEntity, messageEntities)
    }

    private fun tokenizeMessage(messageEntities: List<MessageEntity>): List<TokenMessage> =
        messageEntities.map { message ->
            TokenMessage(
                id = message.id,
                role = message.role?.name,
                content = message.content,
                tokenCount = message.tokenCount,
                createdAt = message.createdAt ?: java.time.LocalDateTime.now()
            )
        }

    data class ContextResult(
        val contextEntity: ContextEntity,
        val messageEntities: List<MessageEntity>
    )
}
