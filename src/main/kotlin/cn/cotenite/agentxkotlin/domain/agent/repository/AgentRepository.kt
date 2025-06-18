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
    
    // 根据创建者查找代理
    fun findByUserIdAndDeletedAtIsNull(userId: String): List<AgentEntity>
    
    // 根据状态查找代理
    fun findByEnabledAndDeletedAtIsNull(enabled: Boolean): List<AgentEntity>
    
    // 根据类型查找代理
    fun findByTypeAndDeletedAtIsNull(type: Int): List<AgentEntity>
    
    // 根据名称模糊搜索
    fun findByNameContainingIgnoreCaseAndDeletedAtIsNull(name: String): List<AgentEntity>
    
    // 根据描述模糊搜索
    fun findByDescriptionContainingIgnoreCaseAndDeletedAtIsNull(description: String): List<AgentEntity>
    
    // 根据创建者和状态查找
    fun findByUserIdAndEnabledAndDeletedAtIsNull(userId: String, enabled: Boolean): List<AgentEntity>
    
    // 查找公开的代理
    fun findByIsPublicTrueAndDeletedAtIsNull(): List<AgentEntity>
    
    // 根据创建者查找公开的代理
    fun findByUserIdAndIsPublicTrueAndDeletedAtIsNull(userId: String): List<AgentEntity>
    
    // 软删除
    @Query("UPDATE AgentEntity a SET a.deletedAt = :deletedAt WHERE a.id = :id")
    fun softDeleteById(@Param("id") id: String, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 批量软删除
    @Query("UPDATE AgentEntity a SET a.deletedAt = :deletedAt WHERE a.id IN :ids")
    fun softDeleteByIds(@Param("ids") ids: List<String>, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 根据ID和用户ID软删除
    @Query("UPDATE AgentEntity a SET a.deletedAt = :deletedAt WHERE a.id = :id AND a.userId = :userId")
    fun softDeleteByIdAndUserId(@Param("id") id: String, @Param("userId") userId: String, @Param("deletedAt") deletedAt: LocalDateTime): Int
    
    // 统计创建者的代理数量
    fun countByUserIdAndDeletedAtIsNull(userId: String): Long
    
    // 统计指定类型的代理数量
    fun countByTypeAndDeletedAtIsNull(type: Int): Long
    
    // 检查代理是否存在
    fun existsByIdAndDeletedAtIsNull(id: String): Boolean
    
    // 根据ID和用户ID查找代理
    fun findByIdAndUserIdAndDeletedAtIsNull(id: String, userId: String): AgentEntity?
    
    // 根据ID列表查找代理
    fun findByIdInAndDeletedAtIsNull(ids: List<String>): List<AgentEntity>
    
    // 查找最近创建的代理
    fun findTop10ByDeletedAtIsNullOrderByCreatedAtDesc(): List<AgentEntity>
    
    // 根据创建者查找最近的代理
    fun findTop10ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId: String): List<AgentEntity>
    
    // 根据标签搜索（假设tags是JSON数组字段）
    @Query("SELECT a FROM AgentEntity a WHERE a.tags @> CAST(:tag AS jsonb) AND a.deletedAt IS NULL", nativeQuery = true)
    fun findByTagsContaining(@Param("tag") tag: String): List<AgentEntity>
    
    // 复合搜索：根据关键词搜索名称或描述
    @Query("SELECT a FROM AgentEntity a WHERE (a.name LIKE CONCAT('%', :keyword, '%') OR a.description LIKE CONCAT('%', :keyword, '%')) AND a.deletedAt IS NULL")
    fun searchByKeyword(@Param("keyword") keyword: String): List<AgentEntity>
    
    // 根据创建时间范围查找
    fun findByCreatedAtBetweenAndDeletedAtIsNull(startTime: LocalDateTime, endTime: LocalDateTime): List<AgentEntity>
    
    // 根据创建者和创建时间范围查找
    fun findByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId: String, startTime: LocalDateTime, endTime: LocalDateTime): List<AgentEntity>
    
    // 根据用户ID查找代理（按更新时间和创建时间倒序）
    fun findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDescCreatedAtDesc(userId: String): List<AgentEntity>
    
    // 根据用户ID和名称模糊搜索代理（按更新时间和创建时间倒序）
    fun findByUserIdAndNameContainingIgnoreCaseAndDeletedAtIsNullOrderByUpdatedAtDescCreatedAtDesc(userId: String, name: String): List<AgentEntity>
}
