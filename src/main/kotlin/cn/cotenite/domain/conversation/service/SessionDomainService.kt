package cn.cotenite.domain.conversation.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import org.springframework.stereotype.Service
import cn.cotenite.domain.conversation.model.SessionEntity
import cn.cotenite.domain.conversation.repository.SessionRepository
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * 会话领域服务
 */
@Service
class SessionDomainService(
    private val sessionRepository: SessionRepository
) {

    fun getSessionsByAgentId(agentId: String): List<SessionEntity> =
        sessionRepository.selectList(
            KtQueryWrapper(SessionEntity::class.java)
                .eq(SessionEntity::agentId, agentId)
                .orderByDesc(SessionEntity::createdAt)
        )

    fun deleteSession(sessionId: String, userId: String) =
        sessionRepository.checkedDelete(
            KtQueryWrapper(SessionEntity::class.java)
                .eq(SessionEntity::id, sessionId)
                .eq(SessionEntity::userId, userId)
        )

    fun updateSession(sessionId: String, userId: String, title: String) =
        sessionRepository.checkedUpdate(
            SessionEntity().apply {
                id = sessionId
                this.userId = userId
                this.title = title
            },
            KtUpdateWrapper(SessionEntity::class.java)
                .eq(SessionEntity::id, sessionId)
                .eq(SessionEntity::userId, userId)
        )

    fun createSession(agentId: String, userId: String): SessionEntity =
        SessionEntity().apply {
            this.agentId = agentId
            this.userId = userId
            title = "新会话"
            sessionRepository.insert(this)
        }

    fun checkSessionExist(sessionId: String, userId: String) {
        find(sessionId, userId) ?: throw BusinessException("会话不存在")
    }

    fun find(sessionId: String, userId: String): SessionEntity? =
        sessionRepository.selectOne(
            KtQueryWrapper(SessionEntity::class.java)
                .eq(SessionEntity::id, sessionId)
                .eq(SessionEntity::userId, userId)
        )

    fun deleteSessions(sessionIds: List<String>) {
        if (sessionIds.isEmpty()) return
        sessionRepository.delete(
            KtQueryWrapper(SessionEntity::class.java)
                .`in`(SessionEntity::id, sessionIds)
        )
    }

    fun getSession(sessionId: String, userId: String): SessionEntity =
        find(sessionId, userId) ?: throw BusinessException("会话不存在")
}
