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


    // 根据AgentId和版本号查找版本
    fun findByAgentIdAndVersionNumber(agentId: String, versionNumber: String): AgentVersionEntity?

    // 根据发布状态查找版本
    fun findByPublishStatus(publishStatus: String): List<AgentVersionEntity>

    // 根据AgentId和用户ID删除版本
    @Modifying
    @Query("DELETE FROM AgentVersionEntity v WHERE v.agentId = :agentId AND v.userId = :userId")
    fun deleteByAgentIdAndUserId(@Param("agentId") agentId: String, @Param("userId") userId: String): Int

    // 查询每个Agent的最新已发布版本，支持名称搜索
    @Query("""SELECT v FROM AgentVersionEntity v 
              WHERE v.publishStatus = :publishStatus 
              AND (:name IS NULL OR v.name LIKE %:name%) 
              AND v.publishedAt = (SELECT MAX(v2.publishedAt) FROM AgentVersionEntity v2 
                                  WHERE v2.agentId = v.agentId AND v2.publishStatus = :publishStatus)""")
    fun findLatestVersionsByNameAndStatus(@Param("name") name: String?, @Param("publishStatus") publishStatus: String): List<AgentVersionEntity>

    // 查询每个Agent的最新版本，按状态过滤
    @Query("""SELECT v FROM AgentVersionEntity v 
              WHERE (:publishStatus IS NULL OR v.publishStatus = :publishStatus) 
              AND v.publishedAt = (SELECT MAX(v2.publishedAt) FROM AgentVersionEntity v2 
                                  WHERE v2.agentId = v.agentId)""")
    fun findLatestVersionsByStatus(@Param("publishStatus") publishStatus: String?): List<AgentVersionEntity>

}
