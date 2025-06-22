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

    // 根据用户ID查找工作区代理
    fun findByUserId(userId: String): List<AgentWorkspaceEntity>

    // 检查代理和用户的工作区是否存在
    fun existsByAgentIdAndUserId(agentId: String, userId: String): Boolean

    // 根据代理ID和用户ID删除工作区
    fun deleteByAgentIdAndUserId(agentId: String, userId: String): Int

    // 根据代理ID和用户ID查找工作区
    fun findByAgentIdAndUserId(agentId: String, userId: String): AgentWorkspaceEntity?

}