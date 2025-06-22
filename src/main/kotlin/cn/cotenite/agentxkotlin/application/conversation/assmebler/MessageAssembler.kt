package cn.cotenite.agentxkotlin.application.conversation.assmebler

import cn.cotenite.agentxkotlin.application.conversation.dto.MessageDTO
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 02:45
 */
object MessageAssembler { // 使用 'object' 关键字创建单例对象，替代 Java 的静态类

    /**
     * 將 Message 實體轉換為 MessageDTO
     *
     * @param message 消息實體
     * @return 消息DTO
     */
    fun toDTO(message: MessageEntity): MessageDTO {
        return message.let {
            MessageDTO(
                id = it.id,
                role = it.role,
                content = it.content,
                createdAt = it.createdAt
            )
        }
    }

    /**
     * 將消息實體列表轉換為DTO列表
     *
     * @param messages 消息實體列表
     * @return 消息DTO列表
     */
    fun toDTOs(messages: List<MessageEntity>): List<MessageDTO> {
        return messages.map { toDTO(it) }
    }
}