package cn.cotenite.domain.conversation.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.repository.MessageRepository

/**
 * 对话领域服务
 */
@Service
class ConversationDomainService(
    private val messageRepository: MessageRepository
) {

    fun getConversationMessages(sessionId: String?): List<MessageEntity> =
        messageRepository.selectList(
            KtQueryWrapper(MessageEntity::class.java)
                .eq(MessageEntity::sessionId, sessionId)
                .orderByAsc(MessageEntity::createdAt)
        )

    fun insertBathMessage(messages: List<MessageEntity>) =
        messageRepository.insert(messages)

    fun saveMessage(message: MessageEntity): MessageEntity =
        message.apply { messageRepository.insert(this) }

    fun deleteConversationMessages(sessionId: String) =
        messageRepository.checkedDelete(
            KtQueryWrapper(MessageEntity::class.java)
                .eq(MessageEntity::sessionId, sessionId)
        )

    fun deleteConversationMessages(sessionIds: List<String>) {
        if (sessionIds.isEmpty()) return
        messageRepository.checkedDelete(
            KtQueryWrapper(MessageEntity::class.java)
                .`in`(MessageEntity::sessionId, sessionIds)
        )
    }

    @Transactional
    fun updateMessageTokenCount(message: MessageEntity) =
        messageRepository.checkedUpdateById(message)
}
