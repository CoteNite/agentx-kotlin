package cn.cotenite.application.agent.service

import org.springframework.stereotype.Service
import cn.cotenite.application.agent.assembler.AgentAssembler
import cn.cotenite.application.agent.assembler.AgentWorkspaceAssembler
import cn.cotenite.application.agent.dto.AgentDTO
import cn.cotenite.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.agent.service.AgentDomainService
import cn.cotenite.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.interfaces.dto.agent.request.UpdateModelConfigRequest

/**
 * Agent工作区应用服务
 */
@Service
class AgentWorkspaceAppService(
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val agentDomainService: AgentDomainService
) {

    fun getAgents(userId: String): List<AgentDTO> =
        agentWorkspaceDomainService.getWorkspaceAgents(userId).let(AgentAssembler::toDTOs)

    fun deleteAgent(agentId: String, userId: String) {
        val agent = agentDomainService.getAgentById(agentId)
        if (agent.userId == userId) throw BusinessException("该助理属于自己，不允许删除")

        if (!agentWorkspaceDomainService.deleteAgent(agentId, userId)) {
            throw BusinessException("删除助理失败")
        }
    }

    fun getConfiguredModelId(agentId: String, userId: String): LLMModelConfig =
        agentWorkspaceDomainService.findWorkspace(agentId, userId)?.llmModelConfig ?: LLMModelConfig()

    fun updateModelConfig(agentId: String, userId: String, request: UpdateModelConfigRequest) {
        val config = AgentWorkspaceAssembler.toLLMModelConfig(request)
        agentWorkspaceDomainService.findWorkspace(agentId, userId)
            ?.apply { llmModelConfig = config }
            ?.also(agentWorkspaceDomainService::update)
            ?: agentWorkspaceDomainService.save(AgentWorkspaceEntity(agentId, userId, config))
    }
}
