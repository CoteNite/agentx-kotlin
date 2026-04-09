package cn.cotenite.domain.tool.repository

import cn.cotenite.domain.tool.model.ToolVersionEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

/**
 * @author  yhk
 * Description  
 * Date  2026/4/6 21:45
 */
@Mapper
interface ToolVersionRepository : MyBatisPlusExtRepository<ToolVersionEntity> {

    @Select(
        """
        SELECT * FROM tool_version t1 
        WHERE t1.public_status = true 
        AND t1.created_at = (
            SELECT MAX(t2.created_at) 
            FROM tool_version t2 
            WHERE t2.tool_id = t1.tool_id AND t2.public_status = true
        )
        """
    )
    fun listLatestPublicToolVersions(): List<ToolVersionEntity>
}