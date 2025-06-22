package cn.cotenite.agentxkotlin.domain.agent.service

import cn.cotenite.agentxkotlin.domain.agent.model.AgentEntity
import cn.cotenite.agentxkotlin.domain.conversation.model.SessionEntity
import cn.cotenite.agentxkotlin.domain.conversation.service.SessionDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import org.springframework.stereotype.Component


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 22:48
 */
@Component
class AgentValidator(
    private val agentDomainService: AgentDomainService,
    private val sessionDomainService: SessionDomainService
){

    fun validateSessionAndAgent(sessionId: String, userId: String): ValidationResult {
        val session = sessionDomainService.getSession(sessionId, userId)
        val agentId = session.agentId?:throw BusinessException("Session not found")
        val agent = agentDomainService.getAgentById(agentId)

        if (agent.userId != userId&&!agent.enabled){
            throw BusinessException("agent已被禁用")
        }

        return ValidationResult(session,agent)
    }

    companion object{

        /**
         * 验证结果
         */
        data class ValidationResult(
            val sessionEntity: SessionEntity?,
            val agentEntity: AgentEntity?
        )
    }
}

