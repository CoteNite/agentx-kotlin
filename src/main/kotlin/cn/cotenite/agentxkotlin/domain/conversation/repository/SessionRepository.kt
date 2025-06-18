package cn.cotenite.agentxkotlin.domain.conversation.repository

import cn.cotenite.agentxkotlin.domain.conversation.model.Session
import org.springframework.data.jpa.repository.JpaRepository
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
interface SessionRepository : JpaRepository<Session, String> {
    
    // 根据用户ID查找会话
    fun findByUserIdAndDeletedAtIsNull(userId: String): List<Session>
    
    // 根据会话名称查找
    fun findByTitleContainingIgnoreCaseAndDeletedAtIsNull(title: String): List<Session>
    
    // 根据状态查找
    fun findByIsArchivedAndDeletedAtIsNull(isArchived: Boolean): List<Session>
    
    // 根据用户ID和状态查找
    fun findByUserIdAndIsArchivedAndDeletedAtIsNull(userId: String, isArchived: Boolean): List<Session>
    
    // 根据创建时间范围查找
    fun findByCreatedAtBetweenAndDeletedAtIsNull(startTime: LocalDateTime, endTime: LocalDateTime): List<Session>
    
    // 根据用户ID和创建时间范围查找
    fun findByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId: String, startTime: LocalDateTime, endTime: LocalDateTime): List<Session>
    
    // 软删除
    @Query("UPDATE Session s SET s.deletedAt = :deletedAt WHERE s.id = :id")
    fun softDeleteById(@Param("id") id: String, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 批量软删除
    @Query("UPDATE Session s SET s.deletedAt = :deletedAt WHERE s.id IN :ids")
    fun softDeleteByIds(@Param("ids") ids: List<String>, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 统计用户的会话数量
    fun countByUserIdAndDeletedAtIsNull(userId: String): Long
    
    // 检查会话是否存在
    fun existsByIdAndDeletedAtIsNull(id: String): Boolean
    
    // 查找用户最近的会话
    fun findTop10ByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(userId: String): List<Session>
    
    // 根据用户ID和名称模糊搜索
    fun findByUserIdAndTitleContainingIgnoreCaseAndDeletedAtIsNull(userId: String, title: String): List<Session>
}
