package cn.cotenite.agentxkotlin.domain.agent.service

import cn.cotenite.agentxkotlin.application.agent.assembler.AgentAssembler
import cn.cotenite.agentxkotlin.domain.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentRepository
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentWorkspaceRepository
import org.springframework.stereotype.Service
import java.util.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/20 23:44
 */
@Service
class AgentWorkspaceDomainService(
    private val agentWorkspaceRepository: AgentWorkspaceRepository,
    private val agentRepository: AgentRepository,
){

    fun getWorkspace(userId: String): List<AgentDTO?>? {
        val agentWorkspaceEntities: MutableList<AgentWorkspaceEntity> = agentWorkspaceRepository.findByUserId(userId)

        val agentIds = agentWorkspaceEntities
            .map(AgentWorkspaceEntity::agentId)
            .toList()

        if (agentIds.isEmpty()) {
            return Collections.emptyList()
        }

        val agents = agentRepository.findAllById(agentIds)

        return agents.map(AgentAssembler::toDTO).toList()
    }

    fun checkAgentWorkspaceExist(agentId: String, userId: String): Boolean{
        return agentWorkspaceRepository.existsAgentWorkspaceEntityByAgentIdAndUserId(agentId, userId)
    }

    fun deleteAgent(agentId: String, userId: String): Boolean {
        return agentWorkspaceRepository.deleteAgentWorkspaceEntityByAgentIdAndUserId(agentId, userId)>0
    }

}
