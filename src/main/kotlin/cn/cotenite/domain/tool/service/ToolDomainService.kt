package cn.cotenite.domain.tool.service

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.model.UserToolEntity
import cn.cotenite.domain.tool.repository.ToolRepository
import cn.cotenite.domain.tool.repository.ToolVersionRepository
import cn.cotenite.domain.tool.repository.UserToolRepository
import cn.cotenite.infrastructure.exception.BusinessException
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/***
 * 工具领域服务
 */
@Service
class ToolDomainService(
    private val toolRepository: ToolRepository,
    private val toolVersionRepository: ToolVersionRepository,
    private val toolStateService: ToolStateDomainService,
    private val userToolRepository: UserToolRepository
) {

    /**
     * 创建工具
     */
    @Transactional
    fun createTool(toolEntity: ToolEntity): ToolEntity {
        return toolEntity.apply {
            status = ToolStatus.WAITING_REVIEW
            mcpServerName = getMcpServerName(this)

            toolRepository.checkInsert(this)
            toolStateService.submitToolForProcessing(this)
        }
    }

    /**
     * 根据 ID 和用户 ID 获取工具
     */
    fun getTool(toolId: String, userId: String): ToolEntity {
        val wrapper = KtQueryWrapper(ToolEntity::class.java)
            .eq(ToolEntity::id, toolId)
            .eq(ToolEntity::userId, userId)

        return toolRepository.selectOne(wrapper)
            ?: throw BusinessException("工具不存在: $toolId")
    }

    /**
     * 获取用户所有工具
     */
    fun getUserTools(userId: String): List<ToolEntity> {
        val queryWrapper = KtQueryWrapper(ToolEntity::class.java)
            .eq(ToolEntity::userId, userId)
            .orderByDesc(ToolEntity::updatedAt)

        return toolRepository.selectList(queryWrapper)
    }

    /**
     * 更新已通过工具的状态
     */
    fun updateApprovedToolStatus(toolId: String, status: ToolStatus): ToolEntity? {
        val wrapper = KtUpdateWrapper(ToolEntity::class.java)
            .eq(ToolEntity::id, toolId)
            .set(ToolEntity::status, status)

        toolRepository.checkedUpdate(wrapper)
        return toolRepository.selectById(toolId)
    }

    /**
     * 更新工具信息
     */
    fun updateTool(toolEntity: ToolEntity): ToolEntity {
        // 获取原工具信息
        val oldTool = toolRepository.selectById(toolEntity.id)
            ?: throw BusinessException("工具不存在: ${toolEntity.id}")

        // 检查是否修改了 URL 或安装命令
        val isUrlChanged = toolEntity.uploadUrl != null && toolEntity.uploadUrl != oldTool.uploadUrl
        val isCommandChanged = toolEntity.installCommand != null && toolEntity.installCommand != oldTool.installCommand

        val needStateTransition = isUrlChanged || isCommandChanged

        if (needStateTransition) {
            toolEntity.mcpServerName = getMcpServerName(toolEntity)
            toolEntity.status = ToolStatus.WAITING_REVIEW
        } else {
            // 只修改了基础信息（name/icon等），仅设为人工审核
            toolEntity.status = ToolStatus.MANUAL_REVIEW
        }

        // 更新工具
        val wrapper = KtUpdateWrapper(ToolEntity::class.java)
            .eq(ToolEntity::id, toolEntity.id)
            .eq(toolEntity.needCheckUserId(), ToolEntity::userId, toolEntity.userId)

        toolRepository.update(toolEntity, wrapper)

        // 如果需要状态流转
        if (needStateTransition) {
            toolStateService.submitToolForProcessing(toolEntity)
        }

        return toolEntity
    }

    /**
     * 删除工具及其关联信息
     */
    @Transactional
    fun deleteTool(toolId: String, userId: String) {
        val toolWrapper = KtQueryWrapper(ToolEntity::class.java)
            .eq(ToolEntity::id, toolId)
            .eq(ToolEntity::userId, userId)

        val userToolWrapper = KtQueryWrapper(UserToolEntity::class.java)
            .eq(UserToolEntity::toolId, toolId)
            .eq(UserToolEntity::userId,userId)

        toolRepository.checkedDelete(toolWrapper)
        userToolRepository.delete(userToolWrapper)
    }

    /**
     * 仅根据 ID 获取工具
     */
    fun getTool(toolId: String): ToolEntity {
        val wrapper = KtQueryWrapper(ToolEntity::class.java)
            .eq(ToolEntity::id, toolId)

        return toolRepository.selectOne(wrapper)
            ?: throw BusinessException("工具不存在: $toolId")
    }

    /**
     * 更新审核失败状态
     */
    fun updateFailedToolStatus(toolId: String, failedStepStatus: ToolStatus, rejectReason: String?): ToolEntity? {
        val wrapper = KtUpdateWrapper(ToolEntity::class.java)
            .eq(ToolEntity::id, toolId)
            .set(ToolEntity::failedStepStatus, failedStepStatus)
            .set(ToolEntity::rejectReason, rejectReason)
            .set(ToolEntity::status, ToolStatus.FAILED)

        toolRepository.checkedUpdate(wrapper)
        return toolRepository.selectById(toolId)
    }

    /**
     * 从安装命令中解析 MCP 服务器名称
     */
    private fun getMcpServerName(tool: ToolEntity?): String? {
        if (tool == null) return null

        val installCommand = tool.installCommand ?: return null

        @Suppress("UNCHECKED_CAST")
        val mcpServers = installCommand["mcpServers"] as? Map<String, Any>
            ?: throw BusinessException("工具ID: ${tool.id} 安装命令中mcpServers为空。")

        return mcpServers.keys.firstOrNull()
            ?: throw BusinessException("工具ID: ${tool.id} 无法从安装命令中获取工具名称。")
    }

    fun getByIds(toolIds: List<String?>): List<ToolEntity>{
        if (toolIds.isEmpty()) {
            return emptyList()
        }
        return toolRepository.selectByIds(toolIds)
    }
}