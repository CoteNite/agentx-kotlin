package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.ContextEntity
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.conversation.repository.ContextRepository
import cn.cotenite.agentxkotlin.domain.conversation.repository.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 对话服务实现
 * @Author RichardYoung
 * @Description Conversation Domain Service
 * @Date 2025/6/16 17:35
 */
@Service
class ConversationDomainService(
    private val messageRepository: MessageRepository,
    private val contextRepository: ContextRepository,
    private val sessionDomainService: SessionDomainService
) {

    private val logger = LoggerFactory.getLogger(ConversationDomainService::class.java)

    /**
     * 获取会话中的消息列表
     *
     * @param sessionId 会话id
     * @return 消息列表
     */
    fun getConversationMessages(sessionId: String): List<MessageEntity> {
        return messageRepository.findBySessionIdAndDeletedAtIsNullOrderByCreatedAtAsc(sessionId)
    }

    /**
     * 批量插入消息
     *
     * @param messages 消息列表
     */
    fun insertBatchMessage(messages: List<MessageEntity>) {
        messageRepository.saveAll(messages)
    }

    /**
     * 保存消息
     *
     * @param message 消息实体
     * @return 保存后的消息实体
     */
    fun saveMessage(message: MessageEntity): MessageEntity {
        return messageRepository.save(message)
    }

    /**
     * 更新上下文，添加新消息到活跃消息列表
     *
     * @param sessionId 会话id
     * @param messageId 消息id
     */
    private fun updateContext(sessionId: String, messageId: String) {
        // 查找当前会话的上下文
        val context = contextRepository.findBySessionId(sessionId)

        if (context == null) {
            // 如果上下文不存在，创建新上下文
            val newContext = ContextEntity.createNew(sessionId)
            newContext.addMessage(messageId)
            contextRepository.save(newContext)
        } else {
            // 更新现有上下文
            context.addMessage(messageId)
            context.updatedAt = LocalDateTime.now()
            contextRepository.save(context)
        }
    }

    /**
     * 删除会话下的消息（软删除）
     *
     * @param sessionId 会话id
     */
    fun deleteConversationMessages(sessionId: String) {
        messageRepository.softDeleteBySessionId(sessionId)
    }

    /**
     * 批量删除会话下的消息（软删除）
     *
     * @param sessionIds 会话id列表
     */
    fun deleteConversationMessages(sessionIds: List<String>) {
        messageRepository.softDeleteBySessionIdIn(sessionIds)
    }

    /**
     * 更新消息的token数量
     *
     * @param message 消息实体
     */
    @Transactional
    fun updateMessageTokenCount(message: MessageEntity) {
        logger.info("更新消息token数量，消息ID: {}, token数量: {}", message.id, message.tokenCount)
        messageRepository.save(message)
    }
}