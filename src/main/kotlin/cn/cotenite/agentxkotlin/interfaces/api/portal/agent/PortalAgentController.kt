package cn.cotenite.agentxkotlin.interfaces.api.portal.agent

import cn.cotenite.agentxkotlin.application.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.application.agent.dto.AgentVersionDTO
import cn.cotenite.agentxkotlin.application.agent.service.AgentAppService
import cn.cotenite.agentxkotlin.infrastructure.auth.UserContext
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import cn.cotenite.agentxkotlin.interfaces.dto.agent.CreateAgentRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.PublishAgentVersionRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.SearchAgentsRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.UpdateAgentRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @Author  RichardYoung
 * @Description  
 * @Date  2025/6/22 18:35
*/
@RestController // 標註為 REST 控制器
@RequestMapping("/agent") // 設定基礎請求路徑
class PortalAgentController(
    private val agentAppService: AgentAppService // 通過構造函數注入 AgentAppService
) {

    /**
     * 創建新 Agent
     */
    @PostMapping
    fun createAgent(@RequestBody @Validated request: CreateAgentRequest): Response<AgentDTO?> {
        val userId = UserContext.getCurrentUserId()
        val agent = agentAppService.createAgent(request, userId)
        return Response.success(agent)
    }

    /**
     * 獲取 Agent 詳情
     */
    @GetMapping("/{agentId}")
    fun getAgent(@PathVariable agentId: String): Response<AgentDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.getAgent(agentId, userId))
    }

    /**
     * 獲取用戶的 Agent 列表，支持可選的狀態和名稱過濾
     */
    @GetMapping("/user")
    fun getUserAgents(searchAgentsRequest: SearchAgentsRequest): Response<List<AgentDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.getUserAgents(userId, searchAgentsRequest))
    }

    /**
     * 獲取已上架的 Agent 列表，支持名稱搜索
     */
    @GetMapping("/published")
    fun getPublishedAgents(searchAgentsRequest: SearchAgentsRequest): Response<List<AgentVersionDTO>> {
        return Response.success(agentAppService.getPublishedAgentsByName(searchAgentsRequest))
    }

    /**
     * 更新 Agent 資訊（基本資訊和配置合併更新）
     */
    @PutMapping("/{agentId}")
    fun updateAgent(
        @PathVariable agentId: String,
        @RequestBody @Validated request: UpdateAgentRequest
    ): Response<AgentDTO?> {
        val userId = UserContext.getCurrentUserId()
        request.agentId = agentId // 直接訪問屬性
        return Response.success(agentAppService.updateAgent(request, userId))
    }

    /**
     * 切換 Agent 的啟用/禁用狀態
     */
    @PutMapping("/{agentId}/toggle-status")
    fun toggleAgentStatus(@PathVariable agentId: String): Response<AgentDTO?> {
        return Response.success(agentAppService.toggleAgentStatus(agentId))
    }

    /**
     * 刪除 Agent
     */
    @DeleteMapping("/{agentId}")
    fun deleteAgent(@PathVariable agentId: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        agentAppService.deleteAgent(agentId, userId)
        return Response.success()
    }

    /**
     * 發布 Agent 版本
     */
    @PostMapping("/{agentId}/publish")
    fun publishAgentVersion(
        @PathVariable agentId: String,
        @RequestBody request: PublishAgentVersionRequest
    ): Response<AgentVersionDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.publishAgentVersion(agentId, request, userId))
    }

    /**
     * 獲取 Agent 的所有版本
     */
    @GetMapping("/{agentId}/versions")
    fun getAgentVersions(@PathVariable agentId: String): Response<List<AgentVersionDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.getAgentVersions(agentId, userId))
    }

    /**
     * 獲取 Agent 的特定版本
     */
    @GetMapping("/{agentId}/versions/{versionNumber}")
    fun getAgentVersion(
        @PathVariable agentId: String,
        @PathVariable versionNumber: String
    ): Response<AgentVersionDTO?> {
        return Response.success(agentAppService.getAgentVersion(agentId, versionNumber))
    }

    /**
     * 獲取 Agent 的最新版本
     */
    @GetMapping("/{agentId}/versions/latest")
    fun getLatestAgentVersion(@PathVariable agentId: String): Response<AgentVersionDTO?> {
        return Response.success(agentAppService.getLatestAgentVersion(agentId))
    }
}