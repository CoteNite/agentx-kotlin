package cn.cotenite.application.conversation.assembler

import cn.cotenite.application.conversation.dto.SessionDTO
import cn.cotenite.domain.conversation.model.SessionEntity
import java.time.LocalDateTime

/**
 * 会话对象转换器
 */
object SessionAssembler {

    fun toDTO(session: SessionEntity): SessionDTO =
        SessionDTO(
            id = session.id,
            title = session.title,
            description = session.description,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            isArchived = session.isArchived,
            agentId = session.agentId
        )

    fun toDTOs(sessions: List<SessionEntity>?): List<SessionDTO> = sessions.orEmpty().map(::toDTO)

    fun toDTO(session: Map<String, Any?>): SessionDTO =
        SessionDTO(
            id = session["id"] as? String,
            title = session["title"] as? String,
            description = session["description"] as? String,
            createdAt = session["createdAt"] as? LocalDateTime,
            updatedAt = session["updatedAt"] as? LocalDateTime,
            isArchived = session["isArchived"] as? Boolean ?: false,
            agentId = session["id"] as? String
        )
}
