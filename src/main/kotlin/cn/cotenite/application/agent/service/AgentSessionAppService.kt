package cn.cotenite.application.agent.service

import org.springframework.stereotype.Service
import cn.cotenite.application.conversation.assembler.MessageAssembler
import cn.cotenite.application.conversation.assembler.SessionAssembler
import cn.cotenite.application.conversation.dto.SessionDTO
import cn.cotenite.domain.agent.service.AgentDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService
import cn.cotenite.domain.conversation.service.SessionDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.interfaces.dto.conversation.ConversationRequest

/**
 * 助理会话应用服务
 */
@Service
class AgentSessionAppService(
    private val agentDomainService: AgentDomainService,
    private val sessionDomainService: SessionDomainService,
    private val conversationDomainService: ConversationDomainService
) {

    fun getAgentSessionList(userId: String, agentId: String): List<SessionDTO> {
        val existsAsOwner = agentDomainService.exist(agentId, userId)
        val existsInWorkspace = agentDomainService.getAgentWithPermissionCheck(agentId, userId).id != null
        if (!existsAsOwner && !existsInWorkspace) throw BusinessException("助理不存在")

        val sessions = sessionDomainService.getSessionsByAgentId(agentId)
            .filter { it.userId == userId }

        return sessions.takeIf { it.isNotEmpty() }
            ?.let(SessionAssembler::toDTOs)
            ?: listOf(SessionAssembler.toDTO(sessionDomainService.createSession(agentId, userId)))
    }

    fun createSession(userId: String, agentId: String): SessionDTO {
        val session = sessionDomainService.createSession(agentId, userId)
        agentDomainService.getAgentById(agentId).welcomeMessage
            ?.takeIf { it.isNotBlank() }
            ?.let { conversationDomainService.saveMessage(MessageAssembler.createSystemMessage(session.id!!, it)) }
        return SessionAssembler.toDTO(session)
    }

    fun updateSession(id: String, userId: String, title: String) =
        sessionDomainService.updateSession(id, userId, title)

    fun deleteSession(id: String, userId: String) =
        sessionDomainService.deleteSession(id, userId)

    fun sendMessage(id: String, userId: String, conversationRequest: ConversationRequest) {
        sessionDomainService.checkSessionExist(id, userId)
        val message = conversationRequest.message ?: throw BusinessException("消息不可为空")
        conversationDomainService.saveMessage(MessageAssembler.createUserMessage(id, message))
    }
}
