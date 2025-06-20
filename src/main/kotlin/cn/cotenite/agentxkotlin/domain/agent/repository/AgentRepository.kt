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

    // 根据ID和用户ID软删除
    @Query("UPDATE AgentEntity a SET a.deletedAt = :deletedAt WHERE a.id = :id AND a.userId = :userId")
    fun softDeleteByIdAndUserId(@Param("id") id: String, @Param("userId") userId: String, @Param("deletedAt") deletedAt: LocalDateTime): Int

    // 根据ID和用户ID查找代理
    fun findByIdAndUserIdAndDeletedAtIsNull(id: String, userId: String): AgentEntity?

    // 根据ID列表查找代理
    fun findByIdInAndDeletedAtIsNull(ids: List<String>): List<AgentEntity>

    // 根据用户ID查找代理（按更新时间和创建时间倒序）
    fun findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDescCreatedAtDesc(userId: String): List<AgentEntity>

    // 根据用户ID和名称模糊搜索代理（按更新时间和创建时间倒序）
    fun findByUserIdAndNameContainingIgnoreCaseAndDeletedAtIsNullOrderByUpdatedAtDescCreatedAtDesc(userId: String, name: String): List<AgentEntity>
}
