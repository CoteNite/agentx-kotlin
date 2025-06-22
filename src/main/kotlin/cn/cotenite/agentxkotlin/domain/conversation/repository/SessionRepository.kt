package cn.cotenite.agentxkotlin.domain.conversation.repository

import cn.cotenite.agentxkotlin.domain.conversation.model.SessionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description Session JPA Repository
 * @Date  2025/6/16 17:30
 */
@Repository
interface SessionRepository : JpaRepository<SessionEntity, String> {

    /**
     * 根据AgentID查找未删除的会话，按创建时间降序排列
     */
    fun findByAgentIdAndDeletedAtIsNullOrderByCreatedAtDesc(agentId: String): List<SessionEntity>

    /**
     * 根据ID和用户ID查找未删除的会话
     */
    fun findByIdAndUserIdAndDeletedAtIsNull(id: String, userId: String): SessionEntity?

    /**
     * 根据ID列表查找未删除的会话
     */
    fun findByIdInAndDeletedAtIsNull(ids: List<String>): List<SessionEntity>

    /**
     * 软删除指定ID和用户ID的会话
     */
    @Modifying
    @Query("UPDATE SessionEntity s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :id AND s.userId = :userId AND s.deletedAt IS NULL")
    fun softDeleteByIdAndUserId(@Param("id") id: String, @Param("userId") userId: String)

    /**
     * 批量软删除指定ID列表的会话
     */
    @Modifying
    @Query("UPDATE SessionEntity s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.id IN :ids AND s.deletedAt IS NULL")
    fun softDeleteByIdIn(@Param("ids") ids: List<String>)
}
