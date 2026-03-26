package cn.cotenite.domain.agent.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import org.springframework.stereotype.Service
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.domain.agent.repository.AgentRepository
import cn.cotenite.domain.agent.repository.AgentWorkspaceRepository
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * Agent工作区领域服务
 */
@Service
class AgentWorkspaceDomainService(
    private val agentWorkspaceRepository: AgentWorkspaceRepository,
    private val agentRepository: AgentRepository
) {

    fun getWorkspaceAgents(userId: String): List<AgentEntity> {
        val agentIds = agentWorkspaceRepository.selectList(
            KtQueryWrapper(AgentWorkspaceEntity::class.java)
                .eq(AgentWorkspaceEntity::userId, userId)
                .select(AgentWorkspaceEntity::agentId)
        ).mapNotNull { it.agentId }

        return agentIds.takeIf { it.isNotEmpty() }
            ?.let(agentRepository::selectByIds)
            ?: emptyList()
    }

    fun exist(agentId: String, userId: String): Boolean =
        agentWorkspaceRepository.exist(agentId, userId)

    fun deleteAgent(agentId: String, userId: String): Boolean =
        agentWorkspaceRepository.delete(
            KtQueryWrapper(AgentWorkspaceEntity::class.java)
                .eq(AgentWorkspaceEntity::agentId, agentId)
                .eq(AgentWorkspaceEntity::userId, userId)
        ) > 0

    fun getWorkspace(agentId: String, userId: String): AgentWorkspaceEntity =
        findWorkspace(agentId, userId) ?: throw BusinessException("助理不存在")

    fun findWorkspace(agentId: String, userId: String): AgentWorkspaceEntity? =
        agentWorkspaceRepository.selectOne(
            KtQueryWrapper(AgentWorkspaceEntity::class.java)
                .eq(AgentWorkspaceEntity::agentId, agentId)
                .eq(AgentWorkspaceEntity::userId, userId)
        )

    fun save(workspace: AgentWorkspaceEntity) = agentWorkspaceRepository.checkInsert(workspace)

    fun update(workspace: AgentWorkspaceEntity) {
        agentWorkspaceRepository.checkedUpdate(
            workspace,
            KtUpdateWrapper(AgentWorkspaceEntity::class.java)
                .eq(AgentWorkspaceEntity::agentId, workspace.agentId)
                .eq(AgentWorkspaceEntity::userId, workspace.userId)
        )
    }
}
