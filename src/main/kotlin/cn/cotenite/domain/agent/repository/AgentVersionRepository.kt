package cn.cotenite.domain.agent.repository

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import cn.cotenite.domain.agent.model.AgentVersionEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * Agent版本仓库接口
 */
@Mapper
interface AgentVersionRepository : MyBatisPlusExtRepository<AgentVersionEntity> {

    @Select(
        """
        <script>
        SELECT a.* FROM agent_versions a
        INNER JOIN (
            SELECT agent_id, MAX(published_at) as max_published_at
            FROM agent_versions
            <if test='publishStatus != null'> WHERE publish_status = #{publishStatus} </if>
            GROUP BY agent_id
        ) b ON a.agent_id = b.agent_id AND a.published_at = b.max_published_at
        <if test='publishStatus != null'> WHERE a.publish_status = #{publishStatus} </if>
        </script>
        """
    )
    fun selectLatestVersionsByStatus(@Param("publishStatus") publishStatus: Int?): List<AgentVersionEntity>

    @Select(
        """
        <script>
        SELECT a.* FROM agent_versions a
        INNER JOIN (
            SELECT agent_id, MAX(published_at) as max_published_at
            FROM agent_versions
            <if test='name != null and name != ""'> WHERE name LIKE CONCAT('%', #{name}, '%') </if>
            GROUP BY agent_id
        ) b ON a.agent_id = b.agent_id AND a.published_at = b.max_published_at
        <if test='name != null and name != ""'> WHERE a.name LIKE CONCAT('%', #{name}, '%') </if>
        </script>
        """
    )
    fun selectLatestVersionsByName(@Param("name") name: String?): List<AgentVersionEntity>

    @Select(
        """
        <script>
        SELECT v.* FROM agent_versions v
        INNER JOIN (
            SELECT agent_id, MAX(published_at) as latest_date
            FROM agent_versions
            WHERE deleted_at IS NULL
            <if test='status != null'> AND publish_status = #{status} </if>
            GROUP BY agent_id
        ) latest ON v.agent_id = latest.agent_id AND v.published_at = latest.latest_date
        WHERE v.deleted_at IS NULL
        <if test='name != null and name != ""'> AND v.name LIKE CONCAT('%', #{name}, '%') </if>
        <if test='status != null'> AND v.publish_status = #{status} </if>
        </script>
        """
    )
    fun selectLatestVersionsByNameAndStatus(
        @Param("name") name: String?,
        @Param("status") status: Int?
    ): List<AgentVersionEntity>
}
