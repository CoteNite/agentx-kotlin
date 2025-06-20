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

    // 根据用户ID查找会话
    fun findByUserIdAndDeletedAtIsNull(userId: String): List<SessionEntity>

    // 根据用户ID和状态查找
    fun findByUserIdAndIsArchivedAndDeletedAtIsNull(userId: String, isArchived: Boolean): List<SessionEntity>

    // 软删除
    @Modifying
    @Query("UPDATE SessionEntity s SET s.deletedAt = :deletedAt WHERE s.id = :id")
    fun softDeleteById(@Param("id") id: String, @Param("deletedAt") deletedAt: LocalDateTime): Int

    // 批量软删除
    @Modifying
    @Query("UPDATE SessionEntity s SET s.deletedAt = :deletedAt WHERE s.id IN :ids")
    fun softDeleteByIds(@Param("ids") ids: List<String>, @Param("deletedAt") deletedAt: LocalDateTime): Int

    // 根据用户ID和名称模糊搜索
    fun findByUserIdAndTitleContainingIgnoreCaseAndDeletedAtIsNull(userId: String, title: String): List<SessionEntity>

    fun getSessionEntitiesByAgentIdOrderByCreatedAtDesc(agentId: String):MutableList<SessionEntity>

    fun deleteByIdIn(sessionIds: List<String>): Int
}
