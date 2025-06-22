package cn.cotenite.agentxkotlin.domain.agent.repository

import cn.cotenite.agentxkotlin.domain.agent.model.AgentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description Agent JPA Repository
 * @Date  2025/6/16 12:00
 */
@Repository
interface AgentRepository : JpaRepository<AgentEntity, String> {

    // 根据ID和用户ID查找Agent
    fun findByIdAndUserId(id: String?, userId: String?): AgentEntity?

    // 根据用户ID查找Agent列表，支持名称模糊查询
    fun findByUserIdAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(userId: String, name: String?): List<AgentEntity>

    // 根据用户ID查找Agent列表
    fun findByUserIdOrderByUpdatedAtDesc(userId: String): List<AgentEntity>

    // 根据ID列表查找Agent
    fun findByIdIn(ids: List<String?>): List<AgentEntity>

    // 根据ID列表和启用状态查找Agent
    fun findByIdInAndEnabled(ids: List<String?>, enabled: Boolean): List<AgentEntity>

    // 检查Agent是否存在
    fun existsByIdAndUserId(id: String, userId: String): Boolean

}
