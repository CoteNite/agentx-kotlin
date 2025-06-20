package cn.cotenite.agentxkotlin.domain.agent.repository

import cn.cotenite.agentxkotlin.domain.agent.model.AgentVersionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description AgentVersion JPA Repository
 * @Date  2025/6/16 12:01
 */
@Repository
interface AgentVersionRepository : JpaRepository<AgentVersionEntity, String> {

    // 根据版本号查找
    fun findByAgentIdAndVersionNumberAndDeletedAtIsNull(agentId: String, versionNumber: String): AgentVersionEntity?


    // 根据代理ID和用户ID查找版本（按发布时间倒序）
    fun findByAgentIdAndUserIdAndDeletedAtIsNullOrderByPublishedAtDesc(agentId: String, userId: String): List<AgentVersionEntity>

    // 根据代理ID和用户ID软删除所有版本
    @Modifying
    @Query("UPDATE AgentVersionEntity av SET av.deletedAt = :deletedAt WHERE av.agentId = :agentId AND av.userId = :userId")
    fun softDeleteByAgentIdAndUserId(@Param("agentId") agentId: String, @Param("userId") userId: String, @Param("deletedAt") deletedAt: LocalDateTime): Int


    // 查找代理的最新版本（按创建时间）
    fun findLatestVersionByAgentId(agentId: String): AgentVersionEntity? {
        return findTop1ByAgentIdAndDeletedAtIsNullOrderByCreatedAtDesc(agentId)
    }

    // 查找代理和用户的最新版本（按创建时间）
    fun findLatestVersionByAgentIdAndUserId(agentId: String, userId: String): AgentVersionEntity? {
        return findTop1ByAgentIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(agentId, userId)
    }

    // 根据代理ID查找最新版本（按创建时间倒序）
    fun findTop1ByAgentIdAndDeletedAtIsNullOrderByCreatedAtDesc(agentId: String): AgentVersionEntity?

    // 根据代理ID和用户ID查找最新版本（按创建时间倒序）
    fun findTop1ByAgentIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(agentId: String, userId: String): AgentVersionEntity?

    /**
     * 查询每个agentId的最新版本（按publishStatus过滤）
     *
     * @param publishStatus 发布状态，为null时查询所有状态
     * @return 每个agentId的最新版本列表
     */
    @Query("""
        SELECT av FROM AgentVersionEntity av 
        WHERE av.id IN (
            SELECT MAX(av2.id) FROM AgentVersionEntity av2 
            WHERE av2.deletedAt IS NULL 
            AND (:publishStatus IS NULL OR av2.publishStatus = :publishStatus)
            GROUP BY av2.agentId
        )
        AND av.deletedAt IS NULL
    """)
    fun selectLatestVersionsByStatus(@Param("publishStatus") publishStatus: Int?): List<AgentVersionEntity>

    /**
     * 根据名称和发布状态查询所有助理的最新版本
     * 同时支持只按状态查询（当name为空时）
     */
    @Query("""
        SELECT av FROM AgentVersionEntity av 
        WHERE av.id IN (
            SELECT MAX(av2.id) FROM AgentVersionEntity av2 
            WHERE av2.deletedAt IS NULL 
            AND (:name IS NULL OR av2.name LIKE %:name%)
            AND (:publishStatus IS NULL OR av2.publishStatus = :publishStatus)
            GROUP BY av2.agentId
        )
        AND av.deletedAt IS NULL
    """)
    fun selectLatestVersionsByNameAndStatus(@Param("name") name: String?, @Param("publishStatus") publishStatus: Int?): List<AgentVersionEntity>

    // 便捷方法：根据名称和发布状态查询最新版本
    fun findLatestVersionsByNameAndPublishStatus(name: String?, publishStatus: Int): List<AgentVersionEntity> {
        return selectLatestVersionsByNameAndStatus(name, publishStatus)
    }

    // 便捷方法：根据发布状态查询最新版本
    fun findLatestVersionsByPublishStatus(publishStatus: Int): List<AgentVersionEntity> {
        return selectLatestVersionsByStatus(publishStatus)
    }
}
