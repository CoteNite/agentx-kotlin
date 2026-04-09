package cn.cotenite.interfaces.api.portal.agent

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.application.agent.dto.AgentDTO
import cn.cotenite.application.agent.dto.AgentVersionDTO
import cn.cotenite.application.agent.service.AgentAppService
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.agent.request.CreateAgentRequest
import cn.cotenite.interfaces.dto.agent.request.PublishAgentVersionRequest
import cn.cotenite.interfaces.dto.agent.request.SearchAgentsRequest
import cn.cotenite.interfaces.dto.agent.request.UpdateAgentRequest

/**
 * 用户Agent管理控制器
 */
@RestController
@RequestMapping("/agent")
class PortalAgentController(
    private val agentAppService: AgentAppService
) {

    @PostMapping
    fun createAgent(@RequestBody @Validated request: CreateAgentRequest): Result<AgentDTO?> =
        Result.success(agentAppService.createAgent(request, currentUserId()))

    @GetMapping("/{agentId}")
    fun getAgent(@PathVariable agentId: String): Result<AgentDTO?> =
        Result.success(agentAppService.getAgent(agentId, currentUserId()))

    @GetMapping("/user")
    fun getUserAgents(searchAgentsRequest: SearchAgentsRequest): Result<List<AgentDTO>> =
        Result.success(agentAppService.getUserAgents(currentUserId(), searchAgentsRequest))

    @GetMapping("/published")
    fun getPublishedAgents(searchAgentsRequest: SearchAgentsRequest): Result<List<AgentVersionDTO>> =
        Result.success(agentAppService.getPublishedAgentsByName(searchAgentsRequest, currentUserId()))

    @PutMapping("/{agentId}")
    fun updateAgent(
        @PathVariable agentId: String,
        @RequestBody @Validated request: UpdateAgentRequest
    ): Result<AgentDTO?> = Result.success(
        agentAppService.updateAgent(request.apply { this.id = agentId }, currentUserId())
    )

    @PutMapping("/{agentId}/toggle-status")
    fun toggleAgentStatus(@PathVariable agentId: String): Result<AgentDTO?> =
        Result.success(agentAppService.toggleAgentStatus(agentId))

    @DeleteMapping("/{agentId}")
    fun deleteAgent(@PathVariable agentId: String): Result<Void> {
        agentAppService.deleteAgent(agentId, currentUserId())
        return Result.success()
    }

    @PostMapping("/{agentId}/publish")
    fun publishAgentVersion(
        @PathVariable agentId: String,
        @RequestBody @Validated request: PublishAgentVersionRequest
    ): Result<AgentVersionDTO?> =
        Result.success(agentAppService.publishAgentVersion(agentId, request, currentUserId()))

    @GetMapping("/{agentId}/versions")
    fun getAgentVersions(@PathVariable agentId: String): Result<List<AgentVersionDTO>> =
        Result.success(agentAppService.getAgentVersions(agentId, currentUserId()))

    @GetMapping("/{agentId}/versions/{versionNumber}")
    fun getAgentVersion(
        @PathVariable agentId: String,
        @PathVariable versionNumber: String
    ): Result<AgentVersionDTO?> = Result.success(agentAppService.getAgentVersion(agentId, versionNumber))

    @GetMapping("/{agentId}/versions/latest")
    fun getLatestAgentVersion(@PathVariable agentId: String): Result<AgentVersionDTO?> =
        Result.success(agentAppService.getLatestAgentVersion(agentId))

    private fun currentUserId(): String = UserContext.getCurrentUserId()
}
