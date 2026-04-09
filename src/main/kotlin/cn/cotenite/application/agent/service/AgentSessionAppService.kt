package cn.cotenite.application.agent.service

import cn.cotenite.application.conversation.assembler.MessageAssembler
import cn.cotenite.application.conversation.assembler.SessionAssembler
import cn.cotenite.application.conversation.dto.SessionDTO
import cn.cotenite.domain.agent.service.AgentDomainService
import cn.cotenite.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService
import cn.cotenite.domain.conversation.service.SessionDomainService
import cn.cotenite.domain.scheduledtask.service.ScheduledTaskExecutionService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.interfaces.dto.conversation.ConversationRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 助理会话应用服务
 */
@Service
class AgentSessionAppService(
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val agentDomainService: AgentDomainService,
    private val sessionDomainService: SessionDomainService,
    private val conversationDomainService: ConversationDomainService,
    private val scheduledTaskExecutionService: ScheduledTaskExecutionService
) {

    fun getAgentSessionList(userId: String, agentId: String): List<SessionDTO> {
        val existsAsOwner = agentDomainService.exist(agentId, userId)
        val existsInWorkspace = agentWorkspaceDomainService.exist(agentId, userId)
        if (!existsAsOwner && !existsInWorkspace) {
            throw BusinessException("助理不存在")
        }

        val sessions = sessionDomainService.getSessionsByAgentId(agentId).filter { it.userId == userId }.toMutableList()
        if (sessions.isEmpty()) {
            sessions.add(sessionDomainService.createSession(agentId, userId))
        }
        return SessionAssembler.toDTOs(sessions)
    }

    fun createSession(userId: String, agentId: String): SessionDTO {
        val session = sessionDomainService.createSession(agentId, userId)
        val agent = agentDomainService.getAgentWithPermissionCheck(agentId, userId)
        agent.welcomeMessage
            ?.takeIf { it.isNotBlank() }
            ?.let { conversationDomainService.saveMessage(MessageAssembler.createSystemMessage(session.id!!, it)) }
        return SessionAssembler.toDTO(session)
    }

    fun updateSession(id: String, userId: String, title: String) =
        sessionDomainService.updateSession(id, userId, title)

    @Transactional
    fun deleteSession(id: String, userId: String) {
        sessionDomainService.deleteSession(id, userId)
        conversationDomainService.deleteConversationMessages(id)
        // 删除定时任务（包括取消延迟队列中的任务）
        scheduledTaskExecutionService.deleteTasksBySessionId(id, userId)
    }

    fun sendMessage(id: String, userId: String, conversationRequest: ConversationRequest) {
        sessionDomainService.checkSessionExist(id, userId)
        val message = conversationRequest.message ?: throw BusinessException("消息不可为空")
        conversationDomainService.saveMessage(MessageAssembler.createUserMessage(id, message))
    }
}
