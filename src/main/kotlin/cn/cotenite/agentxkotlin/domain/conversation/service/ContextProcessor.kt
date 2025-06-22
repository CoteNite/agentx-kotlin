package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.ContextEntity
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.sahred.enums.TokenOverflowStrategyEnum
import cn.cotenite.agentxkotlin.domain.token.model.TokenMessage
import cn.cotenite.agentxkotlin.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.agentxkotlin.domain.token.service.TokenDomainService
import cn.cotenite.agentxkotlin.infrastructure.llm.config.ProviderConfig
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 23:29
 */
@Service
class ContextProcessor(
    private val contextDomainService: ContextDomainService,
    private val messageDomainService: MessageDomainService,
    private val tokenDomainService: TokenDomainService
){

    fun processContext(
        sessionId: String,
        maxTokens: Int,
        strategyType: TokenOverflowStrategyEnum,
        summaryThreshold: Int,
        providerConfig: ProviderConfig
    ): ContextResult {

        var contextEntity = contextDomainService.getBySessionId(sessionId)
        val messageEntities = mutableListOf<MessageEntity>()

        if (contextEntity!=null){
            val activeMessagesIds  = contextEntity.activeMessages
            val messageEntities = messageDomainService.listByIds(activeMessagesIds)

            val tokenizeMessage = this.tokenizeMessage(messageEntities)
            val tokenOverflowConfig = TokenOverflowConfig(
                strategyType = strategyType,
                maxTokens = maxTokens,
                summaryThreshold = summaryThreshold,
                providerConfig = providerConfig
            )

            val tokenProcessResult = tokenDomainService.processMessages(tokenizeMessage, tokenOverflowConfig)

            if (tokenProcessResult.processed){
                val retainedMessages = tokenProcessResult.retainedMessages
                val retainedMessageIds = retainedMessages.map(TokenMessage::id).toMutableList()
                if (strategyType==TokenOverflowStrategyEnum.SUMMARIZE){
                    val newSummary = tokenProcessResult.summary
                    val oldSummary = contextEntity.summary
                    contextEntity.summary = oldSummary+newSummary
                }
                contextEntity.activeMessages=retainedMessageIds
            }
        }else{
            contextEntity = ContextEntity()
            contextEntity.sessionId = sessionId
        }
        return ContextResult(contextEntity,messageEntities)

    }

    private fun tokenizeMessage(messageEntities: MutableList<MessageEntity>): MutableList<TokenMessage>{
        return messageEntities.map { message ->
            TokenMessage(
                id = message.id,
                content = message.content ?: "",
                role = message.role.name,
                tokenCount = message.tokenCount,
                createdAt = message.createdAt
            )
        }.toMutableList()
    }

    data class ContextResult(val contextEntity: ContextEntity, val messageEntities: MutableList<MessageEntity>)

}