package cn.cotenite.domain.agent.repository

import org.apache.ibatis.annotations.Mapper
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * Agent仓库接口
 */
@Mapper
interface AgentRepository : MyBatisPlusExtRepository<AgentEntity>
