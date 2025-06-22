package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.ContextEntity
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.conversation.repository.ContextRepository
import cn.cotenite.agentxkotlin.domain.conversation.repository.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
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
    private val messageRepository: MessageRepository
) {
    companion object {
        private val logger= LoggerFactory.getLogger(ConversationDomainService::class.java)
    }

    /**
     * 获取会话中的消息列表
     *
     * @param sessionId 会话id
     * @return 消息列表，按创建时间升序排列
     */
    fun getConversationMessages(sessionId: String): List<MessageEntity> {
        return messageRepository.findAll { root, query, cb ->
            query?.orderBy(cb.asc(root.get<LocalDateTime>("createdAt")))
            cb.equal(root.get<String>("sessionId"), sessionId)
        }
    }

    /**
     * 批量保存消息
     *
     * @param messages 待保存的消息列表
     */
    @Transactional // 批量保存通常需要事务
    fun saveAllMessages(messages: List<MessageEntity>) {
        messageRepository.saveAll(messages)
    }

    /**
     * 保存单条消息
     *
     * @param message 待保存的消息实体
     * @return 保存后的消息实体（通常包含ID等数据库生成的值）
     */
    @Transactional // 单条保存也通常在事务中
    fun saveMessage(message: MessageEntity): MessageEntity {
        // JPA 的 save 方法会根据ID是否存在自动判断是插入还是更新
        return messageRepository.save(message)
    }

    /**
     * 删除会话下的所有消息
     *
     * @param sessionId 会话id
     */
    @Transactional // 删除操作在事务中
    fun deleteConversationMessages(sessionId: String) {
        messageRepository.deleteBySessionId(sessionId)
    }

    /**
     * 删除多个会话下的消息
     *
     * @param sessionIds 会话ID列表
     */
    @Transactional // 删除操作在事务中
    fun deleteConversationMessages(sessionIds: List<String>) {
        messageRepository.deleteBySessionIds(sessionIds)
    }

    /**
     * 更新消息的token数量
     *
     * @param message 消息实体
     */
    @Transactional // 更新操作在事务中
    fun updateMessageTokenCount(message: MessageEntity) {
        logger.info("更新消息token数量，消息ID: {}, token数量: {}", message.id, message.tokenCount)
        messageRepository.save(message)
    }
}