package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.conversation.repository.MessageRepository
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 23:42
 */
@Service
class MessageDomainService(
    private var messageRepository: MessageRepository
){


    fun listByIds(ids: MutableList<String>): MutableList<MessageEntity> {
        return messageRepository.findAllById(ids)
    }

}