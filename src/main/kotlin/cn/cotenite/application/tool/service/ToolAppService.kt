package cn.cotenite.application.tool.service

import cn.cotenite.application.tool.assembler.ToolAssembler
import cn.cotenite.application.tool.dto.ToolDTO
import cn.cotenite.application.tool.dto.ToolVersionDTO
import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolVersionEntity
import cn.cotenite.domain.tool.model.UserToolEntity
import cn.cotenite.domain.tool.service.ToolDomainService
import cn.cotenite.domain.tool.service.ToolVersionDomainService
import cn.cotenite.domain.tool.service.UserToolDomainService
import cn.cotenite.domain.user.service.UserDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.exception.ParamValidationException
import cn.cotenite.interfaces.dto.tool.request.CreateToolRequest
import cn.cotenite.interfaces.dto.tool.request.MarketToolRequest
import cn.cotenite.interfaces.dto.tool.request.QueryToolRequest
import cn.cotenite.interfaces.dto.tool.request.UpdateToolRequest
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * 工具应用服务
 */
@Service
class ToolAppService(
    private val toolDomainService: ToolDomainService,
    private val userToolDomainService: UserToolDomainService,
    private val toolVersionDomainService: ToolVersionDomainService,
    private val userDomainService: UserDomainService
) {

    private val logger: Logger = LoggerFactory.getLogger(ToolAppService::class.java)


    /**
     * 上传工具
     */
    @Transactional
    fun uploadTool(request: CreateToolRequest, userId: String): ToolDTO {
        val toolEntity = ToolAssembler.toEntity(request, userId).apply {
            status = ToolStatus.WAITING_REVIEW
        }
        val createdTool = toolDomainService.createTool(toolEntity)
        return ToolAssembler.toDTO(createdTool)
    }

    fun getToolDetail(toolId: String, userId: String): ToolDTO {
        val toolEntity = toolDomainService.getTool(toolId, userId)
        return ToolAssembler.toDTO(toolEntity)
    }

    fun getUserTools(userId: String): List<ToolDTO> {
        val toolEntities = toolDomainService.getUserTools(userId)
        return ToolAssembler.toDTOs(toolEntities)
    }

    fun updateTool(toolId: String, request: UpdateToolRequest, userId: String): ToolDTO {
        val toolEntity = ToolAssembler.toEntity(request, userId).apply {
            id = toolId
        }
        val updatedTool = toolDomainService.updateTool(toolEntity)
        return ToolAssembler.toDTO(updatedTool)
    }

    fun deleteTool(toolId: String, userId: String) {
        toolDomainService.deleteTool(toolId, userId)
    }

    @Transactional
    fun marketTool(marketToolRequest: MarketToolRequest, userId: String) {
        val toolId = marketToolRequest.toolId ?: throw ParamValidationException("toolId", "ID不能为空")
        val version = marketToolRequest.version ?: throw ParamValidationException("version", "版本不能为空")

        val toolEntity = toolDomainService.getTool(toolId, userId)
        if (toolEntity.status != ToolStatus.APPROVED) {
            throw BusinessException("工具未审核通过，不能上架")
        }

        val lastVersion = toolVersionDomainService.findLatestToolVersion(toolId, userId)
        if (lastVersion != null) {
            if (!marketToolRequest.isVersionGreaterThan(lastVersion.version)) {
                throw ParamValidationException("versionNumber",
                    "新版本号($version)必须大于当前最新版本号(${lastVersion.version})")
            }
        }

        // 创建新版本实体
        val toolVersionEntity = ToolVersionEntity().apply {
            BeanUtils.copyProperties(toolEntity, this)
            this.version = version
            this.changeLog = marketToolRequest.changeLog
            this.toolId = toolId
            this.publicStatus = true
            this.id = null // 确保是插入新记录
            this.mcpServerName=toolEntity.mcpServerName
            this.createdAt = LocalDateTime.now()
        }
        toolVersionDomainService.addToolVersion(toolVersionEntity)
    }

    fun marketTools(queryToolRequest: QueryToolRequest): Page<ToolVersionDTO> {
        val listToolVersion = toolVersionDomainService.listToolVersion(queryToolRequest)
        val records = listToolVersion.records

        val toolsInstallMap = userToolDomainService.getToolsInstall(records.mapNotNull { it.toolId })

        val dtoList = records.map { entity ->
            ToolAssembler.toDTO(entity).apply {
                installCount = toolsInstallMap[entity.toolId] ?: 0L
            }
        }

        val userNicknameMap = userDomainService
            .getByIds(dtoList.map { it.userId }.toMutableList())
            .associateBy({ it?.id }, { it?.nickname })

        dtoList.forEach(Consumer { toolVersionDTO: ToolVersionDTO? ->
            if (userNicknameMap.containsKey(toolVersionDTO?.userId)) {
                toolVersionDTO?.userName= userNicknameMap[toolVersionDTO.userId]
            }
        })


        return Page<ToolVersionDTO>(listToolVersion.current, listToolVersion.size, listToolVersion.total).apply {
            this.records = dtoList
        }
    }

    fun getToolVersionDetail(toolId: String, version: String, userId: String): ToolVersionDTO {
        val toolVersionEntity = toolVersionDomainService.getToolVersion(toolId, version)

        return ToolAssembler.toDTO(toolVersionEntity).apply {
            // 设置创建者昵称
            val userInfo = userDomainService.getUserInfo(this.userId!!)
            userName = userInfo?.nickname

            // 设置历史版本
            val historyEntities = toolVersionDomainService.getToolVersions(toolId, userId)
            versions = historyEntities.map { ToolAssembler.toDTO(it) }

            // 设置安装量
            val installMap = userToolDomainService.getToolsInstall(listOf(toolId))
            installCount = installMap[toolId] ?: 0L
        }
    }


    fun installTool(toolId: String, version: String, userId: String) {
        val toolVersionEntity = toolVersionDomainService.getToolVersion(toolId, version)
        var userToolEntity = userToolDomainService.findByToolIdAndUserId(toolId, userId)

        val isNew = userToolEntity == null
        val originalId = userToolEntity?.id

        userToolEntity = (userToolEntity ?: UserToolEntity()).apply {
            BeanUtils.copyProperties(toolVersionEntity, this)
            this.userId = userId
            this.toolId = toolVersionEntity.toolId
            this.version = toolVersionEntity.version
            this.id = originalId // 保持原有主键
            this.mcpServerName=toolVersionEntity.mcpServerName
        }

        if (isNew) {
            userToolDomainService.add(userToolEntity)
        } else {
            userToolDomainService.update(userToolEntity)
        }
    }

    fun getInstalledTools(userId: String, queryToolRequest: QueryToolRequest): Page<ToolVersionDTO> {
        val userToolPage = userToolDomainService.listByUserId(userId, queryToolRequest)

        // 查询对应的工具是否还存在
        val toolMap = toolDomainService
            .getByIds(userToolPage.records.mapNotNull { it.toolId })
            .associateBy { it.id }

        val dtoList = userToolPage.records.map { userToolEntity ->
            ToolAssembler.toDTO(userToolEntity).apply {
                if (!toolMap.containsKey(userToolEntity.toolId)) {
                    delete = true
                }
            }
        }

        return Page<ToolVersionDTO>(userToolPage.current, userToolPage.size, userToolPage.total).apply {
            this.records = dtoList
        }
    }

    fun getToolVersions(toolId: String, userId: String): List<ToolVersionDTO> {
        return toolVersionDomainService.getToolVersions(toolId, userId)
            .map { ToolAssembler.toDTO(it) }
    }

    fun uninstallTool(toolId: String, userId: String) {

        // 先检查是否是用户自己创建的工具
        val toolEntity = toolDomainService.getTool(toolId)

        if (toolEntity.userId == userId) {
            // 不允许卸载自己创建的工具
            throw BusinessException("不允许卸载自己创建的工具")
        }
        userToolDomainService.delete(toolId, userId)
    }

    fun getRecommendTools(): List<ToolVersionDTO> {
        val query = QueryToolRequest().apply {
            page = 1
            pageSize = Int.MAX_VALUE
        }
        val pageResult = toolVersionDomainService.listToolVersion(query)
        val records = pageResult.records

        val toolsInstallMap = userToolDomainService.getToolsInstall(records.mapNotNull { it.toolId })

        var dtoList = records.map { entity ->
            ToolAssembler.toDTO(entity).apply {
                installCount = toolsInstallMap[entity.toolId] ?: 0L
            }
        }

        if (dtoList.size > 10) {
            dtoList = dtoList.shuffled().take(10)
        }

        // 批量查询并设置用户昵称
        val userNicknameMap = userDomainService
            .getByIds(dtoList.mapNotNull { it.userId }.toMutableList())
            .filterNotNull()
            .associate { it.id!! to (it.nickname ?: "") }

        dtoList.forEach { dto ->
            userNicknameMap[dto.userId]?.let { dto.userName = it }
        }

        return dtoList
    }

    fun updateUserToolVersionStatus(toolId: String, version: String, publishStatus: Boolean, userId: String) {
        toolVersionDomainService.updateToolVersionStatus(toolId, version, userId, publishStatus)
    }

    /**
     * 为工具创建者自动安装审核通过的工具
     * @param toolId 工具ID
     */
    fun autoInstallApprovedTool(toolId: String) {
        val tool = toolDomainService.getTool(toolId)

        // 确保工具存在且已审核通过
        if (tool.status != ToolStatus.APPROVED) {
            logger.warn("工具ID: {} 不存在或状态不是 APPROVED，无法自动安装。", toolId)
            return
        }

        val ownerId = tool.userId!! // 获取工具创建者ID

        // 检查是否已安装
        val existingInstall = userToolDomainService.findByToolIdAndUserId(toolId, ownerId)
        if (existingInstall != null) {
            logger.info("工具ID: {} 已被用户ID: {} 安装，无需重复自动安装。版本: {}", toolId, ownerId, existingInstall.version)
            return
        }

        // 尝试查找最新已发布的版本，如果为空则创建基础版本
        val versionToInstall = toolVersionDomainService.findLatestToolVersion(toolId, ownerId) ?: run {
            logger.info("工具ID: {} 未找到任何已发布版本，为其创建者 {} 创建一个内部基础版本用于安装。", toolId, ownerId)

            ToolVersionEntity().apply {
                BeanUtils.copyProperties(tool, this) // 从 ToolEntity 复制基础信息
                id = null // 确保是新记录
                this.toolId = toolId
                userId = ownerId // 版本归属创建者
                version = "0.0.0" // 特殊版本号
                changeLog = "Base configuration for owner auto-installation."
                publicStatus = false // 非公开
                mcpServerName = tool.mcpServerName
            }.also { baseVersion ->
                // 持久化这个内部基础版本
                toolVersionDomainService.addToolVersion(baseVersion)
                logger.info("工具ID: {} 已成功创建内部基础版本: {}", toolId, baseVersion.version)
            }
        }

        logger.info("准备为用户ID: {} 安装工具ID: {} 的版本: {}", ownerId, toolId, versionToInstall.version)
        installTool(toolId, versionToInstall.version!!, ownerId)
        logger.info("工具ID: {} 版本: {} 已成功为创建者用户ID: {} 自动安装。", toolId, versionToInstall.version, ownerId)
    }

    /**
     * 根据 toolId 获取最新版本
     */
    fun getLatestToolVersion(toolId: String, userId: String): ToolVersionDTO? {
        return toolVersionDomainService.findLatestToolVersion(toolId, userId)?.let {
            ToolAssembler.toDTO(it)
        }
    }

}