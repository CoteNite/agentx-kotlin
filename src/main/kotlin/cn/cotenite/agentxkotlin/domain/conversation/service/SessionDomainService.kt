package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.application.conversation.assmebler.SessionAssembler
import cn.cotenite.agentxkotlin.domain.conversation.dto.SessionDTO
import cn.cotenite.agentxkotlin.domain.conversation.model.SessionEntity
import cn.cotenite.agentxkotlin.domain.conversation.repository.SessionRepository
import cn.cotenite.agentxkotlin.infrastructure.exception.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.stream.Collectors


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:31
 */
@Service
class SessionDomainService(
    private val sessionRepository: SessionRepository,
    private val contextService: ContextService,
    private val messageService: MessageService,
){

    @Transactional
    fun createSession(title: String, userId: String, description: String): SessionDTO {
        val sessionEntity = SessionEntity.createNew(title, userId)
        sessionEntity.description = description
        val savedSession = sessionRepository.save(sessionEntity)

        contextService.createInitialContext(savedSession.id)

        return savedSession.toDTO()
    }

    fun getSession(sessionId: String): SessionDTO {
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")
        return session.toDTO()
    }

    fun getUserSessions(userId: String): List<SessionDTO> {
        return sessionRepository.findByUserIdAndDeletedAtIsNull(userId)
            .map(SessionEntity::toDTO)
    }

    fun getUserActiveSessions(userId: String): List<SessionDTO> {
        return sessionRepository.findByUserIdAndIsArchivedAndDeletedAtIsNull(userId, false)
            .map(SessionEntity::toDTO)
    }

    fun getUserArchivedSessions(userId: String): List<SessionDTO> {
        return sessionRepository.findByUserIdAndIsArchivedAndDeletedAtIsNull(userId, true)
            .map(SessionEntity::toDTO)
    }

    @Transactional
    fun updateSession(sessionId: String, title: String, description: String): SessionDTO {
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")

        session.update(title, description)
        val updatedSession = sessionRepository.save(session)

        return updatedSession.toDTO()
    }

    @Transactional
    fun archiveSession(sessionId: String): SessionDTO {
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")

        session.archive()
        val updatedSession = sessionRepository.save(session)

        return updatedSession.toDTO()
    }

    @Transactional
    fun unArchiveSession(sessionId: String): SessionDTO {
        val session = sessionRepository.findByIdOrNull(sessionId) 
            ?: throw EntityNotFoundException("会话不存在: $sessionId")

        session.unArchive()
        val updatedSession = sessionRepository.save(session)

        return updatedSession.toDTO()
    }

    @Transactional
    fun deleteSession(sessionId: String) {
        messageService.deleteSessionMessages(sessionId)
        contextService.deleteContext(sessionId)
        sessionRepository.softDeleteById(sessionId, LocalDateTime.now())
    }

    fun searchSessions(userId: String, keyword: String): List<SessionDTO> {
        return sessionRepository.findByUserIdAndTitleContainingIgnoreCaseAndDeletedAtIsNull(userId, keyword)
            .map(SessionEntity::toDTO)
    }

    fun getSessionsByAgentId(agentId: String): List<SessionDTO> {
        val sessions = sessionRepository.getSessionEntitiesByAgentIdOrderByCreatedAtDesc(agentId)
        return sessions.map { SessionAssembler.toDTO(it) }.toList()

    }

    fun deleteSessions(sessionIds: List<String>) {
        sessionRepository.deleteByIdIn(sessionIds)
    }
}
