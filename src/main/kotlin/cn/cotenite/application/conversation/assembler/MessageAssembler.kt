package cn.cotenite.application.conversation.assembler

import cn.cotenite.application.conversation.dto.MessageDTO
import cn.cotenite.domain.conversation.constant.Role
import cn.cotenite.domain.conversation.model.MessageEntity
import org.springframework.beans.BeanUtils
import java.time.LocalDateTime

/**
 * 消息对象转换器
 */
object MessageAssembler {

    fun toDTO(message: MessageEntity?): MessageDTO? = message?.let {
        MessageDTO().apply {
            BeanUtils.copyProperties(message, this)
        }
    }

    fun toDTOs(messages: List<MessageEntity>?): List<MessageDTO> = messages.orEmpty().mapNotNull(::toDTO)

    fun createUserMessage(sessionId: String, content: String): MessageEntity =
        MessageEntity.create(sessionId, Role.USER, content)

    fun createAssistantMessage(sessionId: String, content: String, provider: String?, model: String?): MessageEntity =
        MessageEntity.create(sessionId, Role.ASSISTANT, content).apply {
            this.provider = provider
            this.model = model
        }

    fun createSystemMessage(sessionId: String, content: String): MessageEntity =
        MessageEntity.create(sessionId, Role.SYSTEM, content)

    fun toDTO(message: Map<String, Any?>?): MessageDTO? = message?.let {
        val role = when (val rawRole = it["role"]) {
            is Role -> rawRole
            is String -> Role.entries.firstOrNull { role -> role.name == rawRole }
            else -> null
        }

        MessageDTO(
            id = it["id"] as? String,
            role = role,
            content = it["content"] as? String,
            createdAt = it["createdAt"] as? LocalDateTime,
            provider = it["provider"] as? String,
            model = it["model"] as? String
        )
    }
}
