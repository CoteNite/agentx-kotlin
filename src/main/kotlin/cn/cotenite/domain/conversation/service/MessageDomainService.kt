package cn.cotenite.domain.conversation.service

import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.repository.ContextRepository
import cn.cotenite.domain.conversation.repository.MessageRepository
import org.springframework.stereotype.Service

/**
 * 消息领域服务
 */
@Service
class MessageDomainService(
    private val messageRepository: MessageRepository,
    private val contextRepository: ContextRepository
) {

    fun listByIds(ids: List<String?>): List<MessageEntity> =
        ids.takeIf { it.isNotEmpty() }
            ?.let(messageRepository::selectByIds)
            ?: emptyList()

    fun saveMessageAndUpdateContext(messageEntities: List<MessageEntity>, contextEntity: ContextEntity) {
        messageEntities.takeIf { it.isNotEmpty() }?.let { messages ->
            messages.forEach { it.id = null }
            messageRepository.insert(messages)

            contextEntity.apply {
                activeMessages.addAll(messages.mapNotNull { it.id })
                contextRepository.insertOrUpdate(this)
            }
        }
    }

    fun saveMessage(messageEntities: MutableList<MessageEntity>) = messageRepository.insert(messageEntities)


    fun updateMessage(message: MessageEntity) = messageRepository.updateById(message)

}
