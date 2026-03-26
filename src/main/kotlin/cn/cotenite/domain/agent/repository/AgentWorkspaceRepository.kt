package cn.cotenite.domain.agent.repository

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import cn.cotenite.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * Agent工作区仓库接口
 */
@Mapper
interface AgentWorkspaceRepository : MyBatisPlusExtRepository<AgentWorkspaceEntity> {

    @Select("SELECT EXISTS(SELECT 1 FROM agent_workspace WHERE agent_id = #{agentId} AND user_id = #{userId})")
    fun exist(@Param("agentId") agentId: String, @Param("userId") userId: String): Boolean
}
