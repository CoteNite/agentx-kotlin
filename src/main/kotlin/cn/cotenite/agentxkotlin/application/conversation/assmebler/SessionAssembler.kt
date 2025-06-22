package cn.cotenite.agentxkotlin.application.conversation.assmebler

import cn.cotenite.agentxkotlin.application.conversation.dto.SessionDTO
import cn.cotenite.agentxkotlin.domain.conversation.model.SessionEntity
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 02:47
 */
object SessionAssembler {

    /**
     * 將 SessionEntity 轉換為 SessionDTO
     */
    fun toDTO(session: SessionEntity): SessionDTO {
        return session.let {
            SessionDTO(
                id = it.id?:throw BusinessException("id is null"),
                title = it.title?:throw BusinessException("title is null"),
                agentId = it.agentId,
                description = it.description,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                isArchived = session.isArchived,
            )
        }
    }

    /**
     * 將 SessionDTO 轉換為 SessionEntity
     */
    fun toEntity(sessionDTO: SessionDTO): SessionEntity {
        val sessionEntity = SessionEntity(
            id = sessionDTO.id,
            title = sessionDTO.title,
            agentId = sessionDTO.agentId,
            description = sessionDTO.description,
        )
        sessionEntity.createdAt =sessionDTO.createdAt
        sessionEntity.updatedAt =sessionDTO.updatedAt
        sessionEntity.isArchived=sessionDTO.isArchived
        return sessionEntity
    }

    /**
     * 將 SessionEntity 列表轉換為 SessionDTO 列表
     */
    fun toDTOs(sessions: List<SessionEntity>): List<SessionDTO> {
        return sessions.map { toDTO(it) }
    }
}