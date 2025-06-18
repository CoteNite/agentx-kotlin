package cn.cotenite.agentxkotlin.domain.conversation.repository

import cn.cotenite.agentxkotlin.domain.conversation.model.Context
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description Context JPA Repository
 * @Date  2025/6/16 17:29
 */
@Repository
interface ContextRepository : JpaRepository<Context, String> {
    
    // 根据会话ID查找上下文
    fun findBySessionIdAndDeletedAtIsNull(sessionId: String): Context?
    
    // 根据会话ID查找最新的上下文
    fun findTop1BySessionIdAndDeletedAtIsNullOrderByUpdatedAtDesc(sessionId: String): Context?
    
    // 检查会话是否有上下文
    fun existsBySessionIdAndDeletedAtIsNull(sessionId: String): Boolean
    
    // 软删除
    @Query("UPDATE Context c SET c.deletedAt = :deletedAt WHERE c.id = :id")
    fun softDeleteById(@Param("id") id: String, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 根据会话ID软删除所有上下文
    @Query("UPDATE Context c SET c.deletedAt = :deletedAt WHERE c.sessionId = :sessionId")
    fun softDeleteBySessionId(@Param("sessionId") sessionId: String, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 根据摘要模糊搜索
    fun findBySummaryContainingIgnoreCaseAndDeletedAtIsNull(summary: String): List<Context>
    
    // 根据会话ID和摘要模糊搜索
    fun findBySessionIdAndSummaryContainingIgnoreCaseAndDeletedAtIsNull(sessionId: String, summary: String): List<Context>
    
    // 查找有摘要的上下文
    fun findBySummaryIsNotNullAndDeletedAtIsNull(): List<Context>
    
    // 查找没有摘要的上下文
    fun findBySummaryIsNullAndDeletedAtIsNull(): List<Context>
    
    // 根据更新时间范围查找
    fun findByUpdatedAtBetweenAndDeletedAtIsNull(startTime: LocalDateTime, endTime: LocalDateTime): List<Context>
    
    // 根据会话ID和更新时间范围查找
    fun findBySessionIdAndUpdatedAtBetweenAndDeletedAtIsNull(sessionId: String, startTime: LocalDateTime, endTime: LocalDateTime): List<Context>
    
    // 统计会话的上下文数量
    fun countBySessionIdAndDeletedAtIsNull(sessionId: String): Long
    
    // 检查上下文是否存在
    fun existsByIdAndDeletedAtIsNull(id: String): Boolean
}
