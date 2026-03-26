package cn.cotenite.interfaces.api.portal.agent

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.application.agent.dto.AgentDTO
import cn.cotenite.application.agent.service.AgentWorkspaceAppService
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.agent.request.UpdateModelConfigRequest

/**
 * Agent工作区控制器
 */
@RestController
@RequestMapping("/agent/workspace")
class PortalWorkspaceController(
    private val agentWorkspaceAppService: AgentWorkspaceAppService
) {

    @GetMapping("/agents")
    fun getAgents(): Result<List<AgentDTO>> =
        Result.success(agentWorkspaceAppService.getAgents(currentUserId()))

    @DeleteMapping("/agents/{id}")
    fun deleteAgent(@PathVariable id: String): Result<Void> {
        agentWorkspaceAppService.deleteAgent(id, currentUserId())
        return Result.success()
    }

    @PutMapping("/{agentId}/model/config")
    fun saveModelConfig(
        @RequestBody @Validated config: UpdateModelConfigRequest,
        @PathVariable agentId: String
    ): Result<Void> {
        agentWorkspaceAppService.updateModelConfig(agentId, currentUserId(), config)
        return Result.success()
    }

    @GetMapping("/{agentId}/model-config")
    fun getConfiguredModelId(@PathVariable agentId: String): Result<LLMModelConfig> =
        Result.success(agentWorkspaceAppService.getConfiguredModelId(agentId, currentUserId()))

    private fun currentUserId(): String = UserContext.getCurrentUserId() ?: "anonymous"
}
