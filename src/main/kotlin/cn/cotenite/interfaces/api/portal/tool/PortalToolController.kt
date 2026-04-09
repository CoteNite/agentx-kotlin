package cn.cotenite.interfaces.api.portal.tool

import cn.cotenite.application.tool.dto.ToolDTO
import cn.cotenite.application.tool.dto.ToolVersionDTO
import cn.cotenite.application.tool.service.ToolAppService
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.tool.request.CreateToolRequest
import cn.cotenite.interfaces.dto.tool.request.MarketToolRequest
import cn.cotenite.interfaces.dto.tool.request.QueryToolRequest
import cn.cotenite.interfaces.dto.tool.request.UpdateToolRequest
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.Map

/**
 * 工具市场
 */
@RestController
@RequestMapping("/tools")
class PortalToolController(
    private val toolAppService: ToolAppService
) {

    /**
     * 上传工具
     */
    @PostMapping
    fun createTool(@RequestBody @Validated request: CreateToolRequest): Result<ToolDTO> {
        val userId = UserContext.getCurrentUserId()
        val tool = toolAppService.uploadTool(request, userId)
        return Result.success(tool)
    }

    /**
     * 获取用户的工具详情
     */
    @GetMapping("/{toolId}")
    fun getToolDetail(@PathVariable toolId: String): Result<ToolDTO> {
        val userId = UserContext.getCurrentUserId()
        val tool = toolAppService.getToolDetail(toolId, userId)
        return Result.success(tool)
    }

    /**
     * 获取用户的工具列表
     */
    @GetMapping("/user")
    fun getUserTools(): Result<List<ToolDTO>> {
        val userId = UserContext.getCurrentUserId()
        val tools = toolAppService.getUserTools(userId)
        return Result.success(tools)
    }

    /**
     * 编辑工具
     */
    @PutMapping("/{toolId}")
    fun updateTool(
        @PathVariable toolId: String,
        @RequestBody @Validated request: UpdateToolRequest
    ): Result<ToolDTO> {
        val userId = UserContext.getCurrentUserId()
        val tool = toolAppService.updateTool(toolId, request, userId)
        return Result.success(tool)
    }

    /**
     * 删除工具
     */
    @DeleteMapping("/{toolId}")
    fun deleteTool(@PathVariable toolId: String): Result<Void> {
        val userId = UserContext.getCurrentUserId()
        toolAppService.deleteTool(toolId, userId)
        return Result.success()
    }

    /**
     * 上架工具
     */
    @PostMapping("/market")
    fun marketTool(@RequestBody @Validated marketToolRequest: MarketToolRequest): Result<Unit> {
        val userId = UserContext.getCurrentUserId()
        toolAppService.marketTool(marketToolRequest, userId)
        return Result.success<Unit>().apply {
            message="上架成功"
        }
    }

    /**
     * 工具市场列表
     */
    @GetMapping("/market")
    fun market(queryToolRequest: QueryToolRequest): Result<Page<ToolVersionDTO>> {
        return Result.success(toolAppService.marketTools(queryToolRequest))
    }

    /**
     * 获取工具版本详情
     */
    @GetMapping("/market/{toolId}/{version}")
    fun getToolVersionDetail(
        @PathVariable toolId: String,
        @PathVariable version: String
    ): Result<ToolVersionDTO> {
        val userId = UserContext.getCurrentUserId()
        return Result.success(toolAppService.getToolVersionDetail(toolId, version, userId))
    }

    /**
     * 安装工具
     */
    @PostMapping("/install/{toolId}/{version}")
    fun installTool(@PathVariable toolId: String, @PathVariable version: String): Result<Unit> {
        val userId = UserContext.getCurrentUserId()
        toolAppService.installTool(toolId, version, userId)
        return Result.success<Unit>().apply {
            message="安装成功"
        }
    }

    /**
     * 卸载工具
     */
    @PostMapping("/uninstall/{toolId}")
    fun uninstallTool(@PathVariable toolId: String): Result<Unit> {
        val userId = UserContext.getCurrentUserId()
        toolAppService.uninstallTool(toolId, userId)
        return Result.success<Unit>().apply {
            message="卸载成功"
        }
    }

    /**
     * 获取已安装的工具列表
     */
    @GetMapping("/installed")
    fun getInstalledTools(queryToolRequest: QueryToolRequest): Result<Page<ToolVersionDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Result.success(toolAppService.getInstalledTools(userId, queryToolRequest))
    }

    /**
     * 获取工具已发布的所有版本
     */
    @GetMapping("/market/{toolId}/versions")
    fun getToolVersions(@PathVariable toolId: String): Result<List<ToolVersionDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Result.success(toolAppService.getToolVersions(toolId, userId))
    }

    /**
     * 推荐工具
     */
    @GetMapping("/recommend")
    fun getRecommendTools(): Result<List<ToolVersionDTO>> {
        return Result.success(toolAppService.getRecommendTools())
    }

    /**
     * 修改工具版本发布状态
     */
    @PostMapping("/user/{toolId}/{version}/status")
    fun updateToolVersionStatus(
        @PathVariable toolId: String,
        @PathVariable version: String,
        @RequestParam publishStatus: Boolean
    ): Result<Unit> {
        val userId = UserContext.getCurrentUserId()
        toolAppService.updateUserToolVersionStatus(toolId, version, publishStatus, userId)
        val msg = if (publishStatus) "发布成功" else "下架成功"
        return Result.success<Unit>().apply {
            message=msg
        }
    }

    /** 获取工具最新版本
     * @param toolId 工具id
     * @return
     */
    @GetMapping("/{toolId}/latest")
    fun getLatestToolVersion(@PathVariable toolId: String): Result<kotlin.collections.Map<String, String?>> {
        val userId= UserContext.getCurrentUserId()
        val latestToolVersion = toolAppService.getLatestToolVersion(toolId, userId)
        return Result.success(mapOf("version" to latestToolVersion?.version))
    }
}