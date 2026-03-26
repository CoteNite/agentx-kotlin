package cn.cotenite.application.agent.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import cn.cotenite.application.agent.assembler.AgentAssembler
import cn.cotenite.application.agent.assembler.AgentVersionAssembler
import cn.cotenite.application.agent.dto.AgentDTO
import cn.cotenite.application.agent.dto.AgentVersionDTO
import cn.cotenite.domain.agent.constant.PublishStatus
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.agent.service.AgentDomainService
import cn.cotenite.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.interfaces.dto.agent.CreateAgentRequest
import cn.cotenite.interfaces.dto.agent.PublishAgentVersionRequest
import cn.cotenite.interfaces.dto.agent.ReviewAgentVersionRequest
import cn.cotenite.interfaces.dto.agent.SearchAgentsRequest
import cn.cotenite.interfaces.dto.agent.UpdateAgentRequest

/**
 * Agent应用服务
 */
@Service
class AgentAppService(
    private val agentDomainService: AgentDomainService,
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService
) {

    @Transactional
    fun createAgent(request: CreateAgentRequest, userId: String): AgentDTO? {
        val saved = AgentAssembler.toEntity(request, userId)
            .let(agentDomainService::createAgent)

        agentWorkspaceDomainService.save(AgentWorkspaceEntity(saved.id, userId, LLMModelConfig()))
        return AgentAssembler.toDTO(saved)
    }

    fun getAgent(agentId: String, userId: String): AgentDTO? =
        agentDomainService.getAgent(agentId, userId).let(AgentAssembler::toDTO)

    fun getUserAgents(userId: String, searchAgentsRequest: SearchAgentsRequest): List<AgentDTO> =
        AgentAssembler.toEntity(searchAgentsRequest)
            .let { agentDomainService.getUserAgents(userId, it) }
            .let(AgentAssembler::toDTOs)

    fun getPublishedAgentsByName(searchAgentsRequest: SearchAgentsRequest): List<AgentVersionDTO> =
        AgentEntity().apply { name = searchAgentsRequest.name }
            .let(agentDomainService::getPublishedAgentsByName)
            .let(AgentVersionAssembler::toDTOs)

    fun updateAgent(request: UpdateAgentRequest, userId: String): AgentDTO? =
        AgentAssembler.toEntity(request, userId)
            .let(agentDomainService::updateAgent)
            .let(AgentAssembler::toDTO)

    fun toggleAgentStatus(agentId: String): AgentDTO? =
        agentDomainService.toggleAgentStatus(agentId).let(AgentAssembler::toDTO)

    fun deleteAgent(agentId: String, userId: String) {
        agentDomainService.deleteAgent(agentId, userId)
    }

    fun publishAgentVersion(agentId: String, request: PublishAgentVersionRequest, userId: String): AgentVersionDTO? {
        request.validate()
        val agent = agentDomainService.getAgent(agentId, userId)

        agentDomainService.getLatestAgentVersion(agentId)?.versionNumber?.let {
            if (!request.isVersionGreaterThan(it)) {
                throw BusinessException("新版本号必须大于当前最新版本号")
            }
        }

        return AgentVersionAssembler.createVersionEntity(agent, request)
            .apply { this.userId = userId }
            .let { agentDomainService.publishAgentVersion(agentId, it) }
            .let(AgentVersionAssembler::toDTO)
    }

    fun getAgentVersions(agentId: String, userId: String): List<AgentVersionDTO> =
        agentDomainService.getAgentVersions(agentId, userId).let(AgentVersionAssembler::toDTOs)

    fun getAgentVersion(agentId: String, versionNumber: String): AgentVersionDTO? =
        agentDomainService.getAgentVersion(agentId, versionNumber).let(AgentVersionAssembler::toDTO)

    fun getLatestAgentVersion(agentId: String): AgentVersionDTO? =
        agentDomainService.getLatestAgentVersion(agentId).let(AgentVersionAssembler::toDTO)

    fun reviewAgentVersion(versionId: String, request: ReviewAgentVersionRequest): AgentVersionDTO? {
        request.validate()
        val status = request.status ?: PublishStatus.REVIEWING
        val updated = if (status == PublishStatus.REJECTED) {
            agentDomainService.rejectVersion(versionId, request.rejectReason.orEmpty())
        } else {
            agentDomainService.updateVersionPublishStatus(versionId, status)
        }
        return AgentVersionAssembler.toDTO(updated)
    }

    fun getVersionsByStatus(status: PublishStatus): List<AgentVersionDTO> =
        agentDomainService.getVersionsByStatus(status).let(AgentVersionAssembler::toDTOs)
}
