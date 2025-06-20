package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.infrastructure.exception.EntityNotFoundException
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.conversation.dto.MessageDTO
import cn.cotenite.agentxkotlin.domain.conversation.repository.MessageRepository
import cn.cotenite.agentxkotlin.domain.conversation.repository.SessionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:31
 */
interface MessageService {

    /**
     * 发送用户消息
     */
    fun sendUserMessage(sessionId: String, content: String): MessageDTO

    /**
     * 保存助手响应消息
     */
    fun saveAssistantMessage(
        sessionId: String,
        content: String,
        provider: String,
        model: String,
        tokenCount: Int
    ): MessageDTO

    /**
     * 保存系统消息
     */
    fun saveSystemMessage(sessionId: String, content: String): MessageDTO

    /**
     * 获取会话的所有消息
     */
    fun getSessionMessages(sessionId: String): List<MessageDTO>

    /**
     * 获取会话的最近N条消息
     */
    fun getRecentMessages(sessionId: String, count: Int): List<MessageDTO>

    /**
     * 删除消息
     */
    fun deleteMessage(messageId: String)

    /**
     * 删除会话的所有消息
     */
    fun deleteSessionMessages(sessionId: String)

}

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val sessionRepository: SessionRepository,
    private val contextService: ContextService
) : MessageService {

    @Transactional
    override fun sendUserMessage(sessionId: String, content: String): MessageDTO {
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")

        val messageEntity = MessageEntity.createUserMessage(sessionId, content)
        val savedMessage = messageRepository.save(messageEntity)

        // 更新会话最后更新时间
        session.updatedAt = LocalDateTime.now()
        sessionRepository.save(session)

        // 将消息添加到上下文
        contextService.addMessageToContext(sessionId, savedMessage.id)

        return savedMessage.toDTO()
    }

    @Transactional
    override fun saveAssistantMessage(
        sessionId: String,
        content: String,
        provider: String,
        model: String,
        tokenCount: Int
    ): MessageDTO {
        // 检查会话是否存在
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")

        // 创建并保存助手消息
        val messageEntity = MessageEntity.createAssistantMessage(sessionId, content, provider, model, tokenCount)
        val savedMessage = messageRepository.save(messageEntity)

        // 更新会话最后更新时间
        session.updatedAt = LocalDateTime.now()
        sessionRepository.save(session)

        // 将消息添加到上下文
        contextService.addMessageToContext(sessionId, savedMessage.id)

        return savedMessage.toDTO()
    }

    @Transactional
    override fun saveSystemMessage(sessionId: String, content: String): MessageDTO {
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")

        val messageEntity = MessageEntity.createSystemMessage(sessionId, content)
        val savedMessage = messageRepository.save(messageEntity)

        contextService.addMessageToContext(sessionId, savedMessage.id)

        return savedMessage.toDTO()
    }

    override fun getSessionMessages(sessionId: String): List<MessageDTO> {
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")

        return messageRepository.findBySessionIdAndDeletedAtIsNullOrderByCreatedAtAsc(sessionId)
            .map(MessageEntity::toDTO)
    }

    override fun getRecentMessages(sessionId: String, count: Int): List<MessageDTO> {
        // 检查会话是否存在
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")

        // 获取会话最近的N条消息
        val allMessages = messageRepository.findBySessionIdAndDeletedAtIsNullOrderByCreatedAtAsc(sessionId)
        val recentMessages = if (allMessages.size > count) {
            allMessages.takeLast(count)
        } else {
            allMessages
        }

        return recentMessages.map { it.toDTO() }
    }

    override fun deleteMessage(messageId: String) {
        messageRepository.softDeleteById(messageId, LocalDateTime.now())
    }

    override fun deleteSessionMessages(sessionId: String) {
        messageRepository.softDeleteBySessionId(sessionId, LocalDateTime.now())
    }
}
