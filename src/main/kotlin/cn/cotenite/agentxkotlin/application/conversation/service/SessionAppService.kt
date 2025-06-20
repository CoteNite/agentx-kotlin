package cn.cotenite.agentxkotlin.application.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.dto.SessionDTO
import cn.cotenite.agentxkotlin.domain.conversation.service.SessionDomainService
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/19 00:26
 */
@Service
class SessionAppService(
    private val sessionDomainService: SessionDomainService
){

    /**
     * 创建新会话
     */
    fun createSession(title: String, userId: String, description: String): SessionDTO {
        return sessionDomainService.createSession(title, userId, description)
    }

    /**
     * 获取会话信息
     */
    fun getSession(sessionId: String): SessionDTO {
        return sessionDomainService.getSession(sessionId)
    }

    /**
     * 获取用户的所有会话
     */
    fun getUserSessions(userId: String): List<SessionDTO> {
        return sessionDomainService.getUserSessions(userId)
    }

    /**
     * 获取用户的活跃会话
     */
    fun getUserActiveSessions(userId: String): List<SessionDTO> {
        return sessionDomainService.getUserActiveSessions(userId)
    }

    /**
     * 获取用户的归档会话
     */
    fun getUserArchivedSessions(userId: String): List<SessionDTO> {
        return sessionDomainService.getUserArchivedSessions(userId)
    }

    /**
     * 更新会话信息
     */
    fun updateSession(sessionId: String, title: String, description: String): SessionDTO {
        return sessionDomainService.updateSession(sessionId, title, description)
    }

    /**
     * 归档会话
     */
    fun archiveSession(sessionId: String): SessionDTO {
        return sessionDomainService.archiveSession(sessionId)
    }

    /**
     * 恢复归档会话
     */
    fun unArchiveSession(sessionId: String): SessionDTO {
        return sessionDomainService.unArchiveSession(sessionId)
    }

    /**
     * 删除会话
     */
    fun deleteSession(sessionId: String) {
        sessionDomainService.deleteSession(sessionId)
    }

    /**
     * 搜索会话
     */
    fun searchSessions(userId: String, keyword: String): List<SessionDTO> {
        return sessionDomainService.searchSessions(userId, keyword)
    }

}
