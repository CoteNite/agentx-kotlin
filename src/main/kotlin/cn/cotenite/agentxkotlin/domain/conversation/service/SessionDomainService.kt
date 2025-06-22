package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.SessionEntity
import cn.cotenite.agentxkotlin.domain.conversation.repository.SessionRepository
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * 会话领域服务
 * @Author RichardYoung
 * @Description Session Domain Service
 * @Date 2025/6/16 17:40
 */
@Service
class SessionDomainService(
    private val sessionRepository: SessionRepository
) {

    /**
     * 根据 agentId 获取会话列表
     *
     * @param agentId 助理id
     * @return 会话列表
     */
    fun getSessionsByAgentId(agentId: String): List<SessionEntity> {
        return sessionRepository.findByAgentIdAndDeletedAtIsNullOrderByCreatedAtDesc(agentId)
    }

    /**
     * 删除会话（软删除）
     *
     * @param sessionId 会话id
     * @param userId 用户id
     */
    fun deleteSession(sessionId: String, userId: String) {
        val session = sessionRepository.findByIdAndUserIdAndDeletedAtIsNull(sessionId, userId)
            ?: throw BusinessException("会话不存在")
        
        sessionRepository.softDeleteByIdAndUserId(sessionId, userId)
    }

    /**
     * 更新会话
     *
     * @param sessionId 会话id
     * @param userId 用户id
     * @param title 标题
     */
    fun updateSession(sessionId: String, userId: String, title: String) {
        val session = sessionRepository.findByIdAndUserIdAndDeletedAtIsNull(sessionId, userId)
            ?: throw BusinessException("会话不存在")
        
        session.update(title,session.description)
        sessionRepository.save(session)
    }

    /**
     * 创建会话
     *
     * @param agentId 助理id
     * @param userId 用户id
     * @return 创建的会话实体
     */
    fun createSession(agentId: String, userId: String): SessionEntity {
        val session = SessionEntity(
            title = "新会话",
            userId = userId,
            agentId = agentId
        )
        return sessionRepository.save(session)
    }

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话id
     * @param userId 用户id
     * @throws BusinessException 如果会话不存在
     */
    fun checkSessionExist(sessionId: String, userId: String) {
        val session = sessionRepository.findByIdAndUserIdAndDeletedAtIsNull(sessionId, userId)
        if (session == null) {
            throw BusinessException("会话不存在")
        }
    }

    /**
     * 查找会话
     *
     * @param sessionId 会话id
     * @param userId 用户id
     * @return 会话实体，如果不存在返回null
     */
    fun find(sessionId: String, userId: String): SessionEntity? {
        return sessionRepository.findByIdAndUserIdAndDeletedAtIsNull(sessionId, userId)
    }

    /**
     * 批量删除会话（软删除）
     *
     * @param sessionIds 会话id列表
     */
    fun deleteSessions(sessionIds: List<String>) {
        sessionRepository.softDeleteByIdIn(sessionIds)
    }

    /**
     * 获取会话
     *
     * @param sessionId 会话id
     * @param userId 用户id
     * @return 会话实体
     * @throws BusinessException 如果会话不存在
     */
    fun getSession(sessionId: String, userId: String): SessionEntity {
        return sessionRepository.findByIdAndUserIdAndDeletedAtIsNull(sessionId, userId)
            ?: throw BusinessException("会话不存在")
    }
}