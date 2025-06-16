package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.common.exception.EntityNotFoundException
import cn.cotenite.agentxkotlin.domain.conversation.model.Session
import cn.cotenite.agentxkotlin.domain.conversation.model.SessionDTO
import cn.cotenite.agentxkotlin.domain.conversation.repository.SessionRepository
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:31
 */
interface SessionService {

    /**
     * 创建新会话
     */
    fun createSession(title: String, userId: String, description: String): SessionDTO

    /**
     * 获取单个会话
     */
    fun getSession(sessionId: String): SessionDTO

    /**
     * 获取用户的所有会话
     */
    fun getUserSessions(userId: String): List<SessionDTO>

    /**
     * 获取用户的活跃(非归档)会话
     */
    fun getUserActiveSessions(userId: String): List<SessionDTO>

    /**
     * 获取用户的归档会话
     */
    fun getUserArchivedSessions(userId: String): List<SessionDTO>

    /**
     * 更新会话信息
     */
    fun updateSession(sessionId: String, title: String, description: String): SessionDTO

    /**
     * 归档会话
     */
    fun archiveSession(sessionId: String): SessionDTO

    /**
     * 恢复归档的会话
     */
    fun unArchiveSession(sessionId: String): SessionDTO

    /**
     * 删除会话
     */
    fun deleteSession(sessionId: String)

    /**
     * 搜索会话
     */
    fun searchSessions(userId: String, keyword: String): List<SessionDTO>

}

@Service
class SessionServiceImpl(
    private val sessionRepository: SessionRepository,
    private val contextService: ContextService,
    private val messageService: MessageService,
) : SessionService {

    @Transactional
    override fun createSession(title: String, userId: String, description: String): SessionDTO {
        val session= Session.createNew(title, userId)
        session.description=description
        sessionRepository.insert(session)

        contextService.createInitialContext(session.id)

        return session.toDTO()
    }

    override fun getSession(sessionId: String): SessionDTO {
        val session = sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")
        return session.toDTO()
    }

    override fun getUserSessions(userId: String): List<SessionDTO> {
        val queryWrapper = Wrappers.lambdaQuery<Session>()
            .eq(Session::userId, userId)
            .orderByDesc(Session::updatedAt as SFunction<Session, *>)

        return sessionRepository.selectList(queryWrapper)
            .map(Session::toDTO)
            .toList()
    }

    override fun getUserActiveSessions(userId: String): List<SessionDTO> {
        val queryWrapper = Wrappers.lambdaQuery<Session>()
            .eq(Session::userId, userId)
            .eq(Session::isArchived, false)
            .orderByDesc(Session::updatedAt as SFunction<Session, *>)

        return sessionRepository.selectList(queryWrapper)
            .map(Session::toDTO)
            .toList()
    }
    override fun getUserArchivedSessions(userId: String): List<SessionDTO> {
        val queryWrapper = Wrappers.lambdaQuery<Session>()
            .eq(Session::userId, userId)
            .eq(Session::isArchived, true)
            .orderByDesc(Session::updatedAt as SFunction<Session, *>)

        return sessionRepository.selectList(queryWrapper)
            .map(Session::toDTO)
            .toList()
    }

    @Transactional
    override fun updateSession(sessionId: String, title: String, description: String): SessionDTO {
        val session = sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")

        session.update(title, description)
        sessionRepository.updateById(session)

        return session.toDTO()
    }

    @Transactional
    override fun archiveSession(sessionId: String): SessionDTO {
        val session = sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")

        session.archive()
        sessionRepository.updateById(session)

        return session.toDTO()
    }

    @Transactional
    override fun unArchiveSession(sessionId: String): SessionDTO {
        val session = sessionRepository.selectById(sessionId) ?: throw EntityNotFoundException("会话不存在: $sessionId")

        session.unArchive()
        sessionRepository.updateById(session)

        return session.toDTO()
    }

    @Transactional
    override fun deleteSession(sessionId: String) {
        messageService.deleteSessionMessages(sessionId)
        contextService.deleteContext(sessionId)
        sessionRepository.deleteById(sessionId)
    }

    override fun searchSessions(userId: String, keyword: String): List<SessionDTO> {
        val queryWrapper = Wrappers.lambdaQuery<Session>()
            .eq(Session::userId, userId)
            .like(Session::title, keyword)
            .orderByDesc(Session::updatedAt as SFunction<Session, *>)

        return sessionRepository.selectList(queryWrapper)
            .map(Session::toDTO)
            .toList()
    }
}
