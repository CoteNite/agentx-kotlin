package cn.cotenite.domain.tool.service

import cn.cotenite.domain.tool.model.ToolVersionEntity
import cn.cotenite.domain.tool.repository.ToolVersionRepository
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.interfaces.dto.tool.request.QueryToolRequest
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.springframework.stereotype.Service
import kotlin.collections.maxByOrNull

@Service
class ToolVersionDomainService(
    private val toolVersionRepository: ToolVersionRepository
){

    /**
     * 查询工具版本列表（每个工具只取最新的公开版本）
     */
    fun listToolVersion(queryToolRequest: QueryToolRequest): Page<ToolVersionEntity> {
        val page = queryToolRequest.page
        val pageSize = queryToolRequest.pageSize
        val toolName = queryToolRequest.toolName


        val allPublicList = toolVersionRepository.selectList(
            KtQueryWrapper(ToolVersionEntity::class.java)
                .eq(ToolVersionEntity::publicStatus, true)
                .like(!toolName.isNullOrBlank(), ToolVersionEntity::name, toolName)
                .orderByDesc(ToolVersionEntity::createdAt)
        )

        // 2 & 3. 分组并取每组最新一条，同时按创建时间倒序排列
        val latestList = allPublicList.groupBy { it.toolId }
            .mapNotNull { (_, versions) ->
                versions.filter { it.createdAt != null }.maxByOrNull { it.createdAt!! }
            }
            .sortedByDescending { it.createdAt } // 此时 it 已经是 ToolVersionEntity 且 createdAt 非空

        // 4. 手动分页
        val fromIndex = ((page - 1) * pageSize)
        val toIndex = (fromIndex + pageSize).coerceAtMost(latestList.size)

        val pageList = if (fromIndex >= latestList.size) {
            mutableListOf()
        } else {
            latestList.subList(fromIndex, toIndex)
        }

        return Page<ToolVersionEntity>(page.toLong(), pageSize.toLong(), latestList.size.toLong()).apply {
            records = pageList
        }
    }

    fun getToolVersion(toolId: String, version: String): ToolVersionEntity {

        return toolVersionRepository.selectOne(
            KtQueryWrapper(ToolVersionEntity::class.java)
                .eq(ToolVersionEntity::toolId, toolId)
                .eq(ToolVersionEntity::version, version)

        ) ?: throw BusinessException("工具版本不存在: $toolId $version")
    }

    fun addToolVersion(toolVersionEntity: ToolVersionEntity) {
        toolVersionRepository.insert(toolVersionEntity)
    }

    fun findLatestToolVersion(toolId: String, userId: String? = null): ToolVersionEntity? {
        // Kotlin 允许返回 null，直接返回 selectOne 的结果即可
        return toolVersionRepository.selectOne(
            KtQueryWrapper(ToolVersionEntity::class.java)
                .eq(ToolVersionEntity::toolId, toolId)
                .orderByDesc(ToolVersionEntity::createdAt)
                .last("LIMIT 1")
        )
    }

    /**
     * 获取工具的所有版本
     * 根据当前用户判断：是创建者则返回全量，否则只返回公开版本
     */
    fun getToolVersions(toolId: String, userId: String): List<ToolVersionEntity> {
        // 先查询工具的最新版本以确认创建者
        val latestVersion = toolVersionRepository.selectOne(
            KtQueryWrapper(ToolVersionEntity::class.java)
                .eq(ToolVersionEntity::toolId, toolId)
                .orderByDesc(ToolVersionEntity::createdAt)
                .last("LIMIT 1")
        )
            ?: throw BusinessException("工具版本不存在")

        val queryWrapper = KtQueryWrapper(ToolVersionEntity::class.java)
            .eq(ToolVersionEntity::toolId, toolId)
            .orderByDesc(ToolVersionEntity::createdAt)

        // 权限过滤
        if (userId != latestVersion.userId) {
            queryWrapper.eq(ToolVersionEntity::publicStatus, true)
        }

        return toolVersionRepository.selectList(queryWrapper)
    }

    fun updateToolVersionStatus(toolId: String, version: String, userId: String, publishStatus: Boolean) {
        val wrapper = KtUpdateWrapper(ToolVersionEntity::class.java)
            .eq(ToolVersionEntity::toolId, toolId)
            .eq(ToolVersionEntity::version, version)
            .eq(ToolVersionEntity::userId, userId)
            .set(ToolVersionEntity::publicStatus, publishStatus)

        toolVersionRepository.checkedUpdate(wrapper)
    }

}