package cn.cotenite.domain.conversation.service

import org.springframework.stereotype.Service
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.repository.MessageRepository

/**
 * 消息领域服务
 */
@Service
class MessageDomainService(
    private val messageRepository: MessageRepository
) {

    fun listByIds(ids: List<String>): List<MessageEntity> =
        ids.takeIf { it.isNotEmpty() }
            ?.let(messageRepository::selectByIds)
            ?: emptyList()
}
