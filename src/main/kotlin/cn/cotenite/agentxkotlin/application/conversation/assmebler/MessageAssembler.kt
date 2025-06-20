package cn.cotenite.agentxkotlin.application.conversation.assmebler

import cn.cotenite.agentxkotlin.domain.conversation.dto.MessageDTO
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 02:45
 */
object MessageAssembler { // 使用 'object' 关键字创建单例对象，替代 Java 的静态类

    /**
     * 将Message实体转换为MessageDTO
     *
     * @param message 消息实体
     * @return 消息DTO
     */
    fun toDTO(message: MessageEntity): MessageDTO? {
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
     * 将消息实体列表转换为DTO列表
     *
     * @param messages 消息实体列表
     * @return 消息DTO列表
     */
    fun toDTOList(messages: List<MessageEntity>): List<MessageDTO> {
        return messages.mapNotNull { toDTO(it) }
    }
}