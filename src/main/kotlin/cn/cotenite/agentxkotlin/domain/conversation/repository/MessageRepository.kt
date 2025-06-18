package cn.cotenite.agentxkotlin.domain.conversation.repository

import cn.cotenite.agentxkotlin.domain.conversation.model.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description Message JPA Repository
 * @Date  2025/6/16 17:29
 */
@Repository
interface MessageRepository : JpaRepository<Message, String> {
    
    // 根据会话ID查找消息
    fun findBySessionIdAndDeletedAtIsNullOrderByCreatedAtAsc(sessionId: String): List<Message>
    
    // 根据角色查找消息
    fun findByRoleAndDeletedAtIsNull(role: String): List<Message>
    
    // 根据会话ID和角色查找消息
    fun findBySessionIdAndRoleAndDeletedAtIsNull(sessionId: String, role: String): List<Message>
    
    // 根据内容模糊搜索
    fun findByContentContainingIgnoreCaseAndDeletedAtIsNull(content: String): List<Message>
    
    // 根据会话ID和内容模糊搜索
    fun findBySessionIdAndContentContainingIgnoreCaseAndDeletedAtIsNull(sessionId: String, content: String): List<Message>
    
    // 根据时间范围查找消息
    fun findByCreatedAtBetweenAndDeletedAtIsNull(startTime: LocalDateTime, endTime: LocalDateTime): List<Message>
    
    // 根据会话ID和时间范围查找消息
    fun findBySessionIdAndCreatedAtBetweenAndDeletedAtIsNull(sessionId: String, startTime: LocalDateTime, endTime: LocalDateTime): List<Message>
    
    // 根据提供商查找消息
    fun findByProviderAndDeletedAtIsNull(provider: String): List<Message>
    
    // 根据模型查找消息
    fun findByModelAndDeletedAtIsNull(model: String): List<Message>
    
    // 软删除
    @Query("UPDATE Message m SET m.deletedAt = :deletedAt WHERE m.id = :id")
    fun softDeleteById(@Param("id") id: String, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 批量软删除
    @Query("UPDATE Message m SET m.deletedAt = :deletedAt WHERE m.id IN :ids")
    fun softDeleteByIds(@Param("ids") ids: List<String>, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 根据会话ID软删除所有消息
    @Query("UPDATE Message m SET m.deletedAt = :deletedAt WHERE m.sessionId = :sessionId")
    fun softDeleteBySessionId(@Param("sessionId") sessionId: String, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 统计会话的消息数量
    fun countBySessionIdAndDeletedAtIsNull(sessionId: String): Long
    
    // 统计会话中指定角色的消息数量
    fun countBySessionIdAndRoleAndDeletedAtIsNull(sessionId: String, role: String): Long
    
    // 检查消息是否存在
    fun existsByIdAndDeletedAtIsNull(id: String): Boolean
    
    // 查找会话的最新消息
    fun findTop1BySessionIdAndDeletedAtIsNullOrderByCreatedAtDesc(sessionId: String): Message?
    
    // 统计token总数
    @Query("SELECT COALESCE(SUM(m.tokenCount), 0) FROM Message m WHERE m.sessionId = :sessionId AND m.deletedAt IS NULL")
    fun sumTokensBySessionId(@Param("sessionId") sessionId: String): Long
    
    // 统计指定时间范围内的token总数
    @Query("SELECT COALESCE(SUM(m.tokenCount), 0) FROM Message m WHERE m.createdAt BETWEEN :startTime AND :endTime AND m.deletedAt IS NULL")
    fun sumTokensByTimeRange(@Param("startTime") startTime: LocalDateTime, @Param("endTime") endTime: LocalDateTime): Long
}
