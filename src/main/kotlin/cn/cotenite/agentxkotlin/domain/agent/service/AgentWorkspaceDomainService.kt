package cn.cotenite.agentxkotlin.domain.agent.service

import cn.cotenite.agentxkotlin.domain.agent.model.AgentEntity
import cn.cotenite.agentxkotlin.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentRepository
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentWorkspaceRepository
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import org.springframework.stereotype.Service

@Service
class AgentWorkspaceDomainService(
    private val agentWorkspaceRepository: AgentWorkspaceRepository,
    private val agentRepository: AgentRepository
) {

    fun getWorkspaceAgents(userId: String): List<AgentEntity> {
        val workspaces = agentWorkspaceRepository.findByUserId(userId)
        val agentIds = workspaces.map { it.agentId }

        if (agentIds.isEmpty()) {
            return emptyList()
        }
        return agentRepository.findByIdIn(agentIds)
    }

    fun exist(agentId: String, userId: String): Boolean {
        return agentWorkspaceRepository.existsByAgentIdAndUserId(agentId, userId)
    }

    fun deleteAgent(agentId: String, userId: String): Boolean {
        return agentWorkspaceRepository.deleteByAgentIdAndUserId(agentId, userId) > 0
    }

    fun getWorkspace(agentId: String, userId: String): AgentWorkspaceEntity {
        return agentWorkspaceRepository.findByAgentIdAndUserId(agentId, userId)
            ?: throw BusinessException("助理不存在")
    }

    fun findWorkspace(agentId: String, userId: String): AgentWorkspaceEntity? {
        return agentWorkspaceRepository.findByAgentIdAndUserId(agentId, userId)
    }

    fun save(workspace: AgentWorkspaceEntity) {
        try {
            agentWorkspaceRepository.save(workspace)
        } catch (e: Exception) {
            throw BusinessException("保存失败: ${e.message}")
        }
    }
}