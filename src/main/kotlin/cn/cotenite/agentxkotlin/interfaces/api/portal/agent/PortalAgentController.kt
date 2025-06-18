package cn.cotenite.agentxkotlin.interfaces.api.portal.agent

import cn.cotenite.agentxkotlin.application.agent.service.AgentAppService
import cn.cotenite.agentxkotlin.domain.agent.model.AgentDTO
import cn.cotenite.agentxkotlin.domain.agent.model.AgentVersionDTO
import cn.cotenite.agentxkotlin.infrastructure.auth.UserContext
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import cn.cotenite.agentxkotlin.interfaces.dto.agent.CreateAgentRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.PublishAgentVersionRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.SearchAgentsRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.UpdateAgentRequest
import org.springframework.web.bind.annotation.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/19 00:32
 */
@RestController
@RequestMapping("/agent")
class PortalAgentController(
    private val agentAppService: AgentAppService
){

    /**
     * 创建新Agent
     */
    @PostMapping
    fun createAgent(@RequestBody request: CreateAgentRequest): Response<AgentDTO> {
        val userId = UserContext.getCurrentUserId()
        val agent = agentAppService.createAgent(request, userId)
        return Response.success(agent)
    }

    /**
     * 获取Agent详情
     */
    @GetMapping("/{agentId}")
    fun getAgent(@PathVariable agentId: String): Response<AgentDTO> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.getAgent(agentId, userId))
    }

    /**
     * 获取用户的Agent列表，支持可选的状态和名称过滤
     */
    @GetMapping("/user")
    fun getUserAgents(
        searchAgentsRequest: SearchAgentsRequest
    ): Response<List<AgentDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.getUserAgents(userId, searchAgentsRequest))
    }

    /**
     * 获取已上架的Agent列表，支持名称搜索
     */
    @GetMapping("/published")
    fun getPublishedAgents(searchAgentsRequest: SearchAgentsRequest): Response<List<AgentVersionDTO>> {
        return Response.success(agentAppService.getPublishedAgentsByName(searchAgentsRequest))
    }

    /**
     * 更新Agent信息（基本信息和配置合并更新）
     */
    @PutMapping("/{agentId}")
    fun updateAgent(
        @PathVariable agentId: String,
        @RequestBody request: UpdateAgentRequest
    ): Response<AgentDTO> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.updateAgent(agentId, request, userId))
    }

    /**
     * 切换Agent的启用/禁用状态
     */
    @PutMapping("/{agentId}/toggle-status")
    fun toggleAgentStatus(@PathVariable agentId: String): Response<AgentDTO> {
        return Response.success(agentAppService.toggleAgentStatus(agentId))
    }

    /**
     * 删除Agent
     */
    @DeleteMapping("/{agentId}")
    fun deleteAgent(@PathVariable agentId: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        agentAppService.deleteAgent(agentId, userId)
        return Response.success()
    }

    /**
     * 发布Agent版本
     */
    @PostMapping("/{agentId}/publish")
    fun publishAgentVersion(
        @PathVariable agentId: String,
        @RequestBody request: PublishAgentVersionRequest
    ): Response<AgentVersionDTO> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.publishAgentVersion(agentId, request, userId))
    }

    /**
     * 获取Agent的所有版本
     */
    @GetMapping("/{agentId}/versions")
    fun getAgentVersions(@PathVariable agentId: String): Response<List<AgentVersionDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentAppService.getAgentVersions(agentId, userId))
    }

    /**
     * 获取Agent的特定版本
     */
    @GetMapping("/{agentId}/versions/{versionNumber}")
    fun getAgentVersion(
        @PathVariable agentId: String,
        @PathVariable versionNumber: String
    ): Response<AgentVersionDTO> {
        return Response.success(agentAppService.getAgentVersion(agentId, versionNumber))
    }

    /**
     * 获取Agent的最新版本
     */
    @GetMapping("/{agentId}/versions/latest")
    fun getLatestAgentVersion(@PathVariable agentId: String): Response<AgentVersionDTO> {
        return Response.success(agentAppService.getLatestAgentVersion(agentId))
    }

}
