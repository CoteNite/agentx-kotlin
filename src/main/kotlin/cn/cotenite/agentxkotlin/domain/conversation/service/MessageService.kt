package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.common.exception.EntityNotFoundException
import cn.cotenite.agentxkotlin.domain.conversation.model.Message
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageDTO
import cn.cotenite.agentxkotlin.domain.conversation.repository.MessageRepository
import cn.cotenite.agentxkotlin.domain.conversation.repository.SessionRepository
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
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
        sessionId: String, content: String, provider: String, model: String,
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
        val session =sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")


        val message = Message.createUserMessage(sessionId, content)
        messageRepository.insert(message)


        // 更新会话最后更新时间
        session.updatedAt=LocalDateTime.now()
        sessionRepository.updateById(session)


        // 将消息添加到上下文
        contextService.addMessageToContext(sessionId, message.id)

        return message.toDTO()
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
        val session = sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")


        // 创建并保存助手消息
        val message = Message.createAssistantMessage(sessionId, content, provider, model, tokenCount)
        messageRepository.insert(message)


        // 更新会话最后更新时间
        session.updatedAt=LocalDateTime.now()
        sessionRepository.updateById(session)


        // 将消息添加到上下文
        contextService.addMessageToContext(sessionId, message.id)

        return message.toDTO()
    }

    @Transactional
    override fun saveSystemMessage(sessionId: String, content: String): MessageDTO {

        val session = sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")

        val message = Message.createSystemMessage(sessionId, content)
        messageRepository.insert(message)

        contextService.addMessageToContext(sessionId, message.id)

        return message.toDTO()
    }

    override fun getSessionMessages(sessionId: String): List<MessageDTO> {

        val session = sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")


        val queryWrapper = Wrappers.lambdaQuery<Message>()
            .eq(Message::sessionId, sessionId)
            .orderByAsc(Message::createdAt as SFunction<Message, *>)

        val messages = messageRepository.selectList(queryWrapper)
        return messages.map(Message::toDTO).toList()
    }

    override fun getRecentMessages(sessionId: String, count: Int): List<MessageDTO> {

        // 检查会话是否存在
        val session = sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")


        // 使用LambdaQueryWrapper获取会话最近的N条消息，使用last方法限制条数
        val queryWrapper = Wrappers.lambdaQuery<Message>()
            .eq(Message::sessionId, sessionId)
            .orderByDesc(Message::createdAt as SFunction<Message, *>)
            .last("LIMIT $count")

        val recentMessages = messageRepository.selectList(queryWrapper)

        return recentMessages.reversed().map { it.toDTO() }
    }

    override fun deleteMessage(messageId: String) {
        messageRepository.deleteById(messageId)
    }

    override fun deleteSessionMessages(sessionId: String) {
        val wrapper = Wrappers.lambdaQuery<Message>().eq(Message::sessionId, sessionId)
        messageRepository.delete(wrapper)
    }
}
