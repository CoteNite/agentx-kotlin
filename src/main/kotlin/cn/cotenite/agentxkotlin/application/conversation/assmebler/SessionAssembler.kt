package cn.cotenite.agentxkotlin.application.conversation.assmebler

import cn.cotenite.agentxkotlin.domain.conversation.dto.SessionDTO
import cn.cotenite.agentxkotlin.domain.conversation.model.SessionEntity

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 02:47
 */
object SessionAssembler {

    /**
     * 将 Session 实体转换为 SessionDTO
     *
     * @param session 消息实体
     * @return 消息DTO
     */
    fun toDTO(session: SessionEntity): SessionDTO {
        // 直接在构造函数中赋值，无需先创建对象再逐个设置属性
        return SessionDTO(
            id = session.id,
            title = session.title,
            agentId = session.agentId,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            description = session.description,
            isArchived = session.isArchived
        )
    }

    /**
     * 将 Session DTO 转换为 Session 实体
     *
     * @param sessionDTO 消息DTO
     * @return 消息实体
     */
    fun toEntity(sessionDTO: SessionDTO): SessionEntity {
        // 同样直接在构造函数中赋值
        return SessionEntity(
            id = sessionDTO.id,
            title = sessionDTO.title,
            agentId = sessionDTO.agentId,
            createdAt = sessionDTO.createdAt,
            updatedAt = sessionDTO.updatedAt,
            description = sessionDTO.description,
            isArchived = sessionDTO.isArchived,
            userId = "",
        )
    }

}