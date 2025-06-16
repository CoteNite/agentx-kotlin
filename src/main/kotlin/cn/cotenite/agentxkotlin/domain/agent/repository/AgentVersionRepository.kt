package cn.cotenite.agentxkotlin.domain.agent.repository

import cn.cotenite.agentxkotlin.domain.agent.model.AgentVersionEntity
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.apache.ibatis.annotations.Mapper


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 12:01
 */
@Mapper
interface AgentVersionRepository: BaseMapper<AgentVersionEntity> {

    /**
     * 查询每个agentId的最新版本（按publishStatus过滤）
     *
     * @param publishStatus 发布状态，为null时查询所有状态
     * @return 每个agentId的最新版本列表
     *
     */
    fun selectLatestVersionsByStatus(publishStatus: Int?): List<AgentVersionEntity>

    /**
     * 按名称搜索每个agentId的最新版本
     *
     * @param name 搜索的名称，模糊匹配
     * @return 符合条件的每个agentId的最新版本列表
     */
    fun selectLatestVersionsByName(name: String?): List<AgentVersionEntity>

    /**
     * 根据名称和发布状态查询所有助理的最新版本
     * 同时支持只按状态查询（当name为空时）
     */
    fun selectLatestVersionsByNameAndStatus(name: String?, status: Int?): List<AgentVersionEntity>

}
