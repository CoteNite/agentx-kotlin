package cn.cotenite.domain.conversation.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.service.ChatCompletionHandler
import cn.cotenite.domain.conversation.service.ContextDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * 聊天完成处理器实现
 */
@Service
class ChatCompletionHandlerImpl(
    private val conversationDomainService: ConversationDomainService,
    private val contextDomainService: ContextDomainService
) : ChatCompletionHandler {

    @Transactional(rollbackFor = [Exception::class])
    override fun handleCompletion(
        userMessage: MessageEntity,
        llmMessage: MessageEntity,
        contextEntity: ContextEntity,
        inputTokenCount: Int?,
        outputTokenCount: Int?,
        llmContent: String
    ) {
        runCatching {
            userMessage.tokenCount = inputTokenCount ?: 0
            llmMessage.tokenCount = outputTokenCount ?: 0
            llmMessage.content = llmContent

            conversationDomainService.insertBathMessage(listOf(userMessage, llmMessage))

            contextEntity.activeMessages.add(userMessage.id)
            contextEntity.activeMessages.add(llmMessage.id)
            contextDomainService.insertOrUpdate(contextEntity)
        }.getOrElse {
            throw BusinessException("处理聊天完成逻辑失败: ${it.message}", it)
        }
    }
}
