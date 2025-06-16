package cn.cotenite.agentxkotlin.domain.conversation.model

import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:17
 */
data class SessionDTO(
    val id:String,
    val title:String,
    val description: String?,
    val createdAt:LocalDateTime,
    val updatedAt:LocalDateTime,
    val isArchived:Boolean
){
    companion object{
//        fun fromEntity(session: Session): SessionDTO {
//            val dto = SessionDTO()
//            dto.setId(session.getId())
//            dto.setTitle(session.getTitle())
//            dto.setDescription(session.getDescription())
//            dto.setCreatedAt(session.getCreatedAt())
//            dto.setUpdatedAt(session.getUpdatedAt())
//            dto.setArchived(session.isArchived())
//            return dto
//        }
    }
}
