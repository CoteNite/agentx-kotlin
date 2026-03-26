package cn.cotenite.domain.agent.service

import org.springframework.stereotype.Component
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.conversation.model.SessionEntity
import cn.cotenite.domain.conversation.service.SessionDomainService
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * Agent验证器
 */
@Component
class AgentValidator(
    private val sessionDomainService: SessionDomainService,
    private val agentDomainService: AgentDomainService
) {

    fun validateSessionAndAgent(sessionId: String, userId: String): ValidationResult {
        val session = sessionDomainService.getSession(sessionId, userId)
        val agentId = session.agentId ?: throw BusinessException("会话未绑定助理")

        val agent = agentDomainService.getAgentById(agentId)
        if (agent.userId != userId && !agent.enabled) {
            throw BusinessException("agent已被禁用")
        }

        return ValidationResult(session, agent)
    }

    data class ValidationResult(
        val sessionEntity: SessionEntity,
        val agentEntity: AgentEntity
    )
}
