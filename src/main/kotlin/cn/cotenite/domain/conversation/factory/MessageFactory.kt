package cn.cotenite.domain.conversation.factory

import org.springframework.stereotype.Component
import cn.cotenite.domain.conversation.constant.Role
import cn.cotenite.domain.conversation.model.MessageEntity

/**
 * 消息工厂
 */
@Component
class MessageFactory {

    fun createUserMessage(content: String, sessionId: String): MessageEntity =
        MessageEntity.create(sessionId, Role.USER, content)

    fun createSystemMessage(sessionId: String, modelId: String?, providerId: String?): MessageEntity =
        MessageEntity.create(sessionId, Role.ASSISTANT, "").apply {
            model = modelId
            provider = providerId
        }
}
