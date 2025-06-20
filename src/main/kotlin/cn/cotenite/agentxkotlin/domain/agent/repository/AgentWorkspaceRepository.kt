package cn.cotenite.agentxkotlin.domain.agent.repository

import cn.cotenite.agentxkotlin.domain.agent.model.AgentWorkspaceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/20 23:32
 */
@Repository
interface AgentWorkspaceRepository : JpaRepository<AgentWorkspaceEntity, String>{
    fun findByUserId(userId: String): MutableList<AgentWorkspaceEntity>
    fun existsAgentWorkspaceEntityByAgentIdAndUserId(agentId: String, userId: String): Boolean
    fun deleteAgentWorkspaceEntityByAgentIdAndUserId(agentId: String, userId: String): Int

}