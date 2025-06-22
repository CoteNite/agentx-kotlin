package cn.cotenite.agentxkotlin.interfaces.api.portal.agent

import cn.cotenite.agentxkotlin.application.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.application.agent.service.AgentWorkspaceAppService
import cn.cotenite.agentxkotlin.infrastructure.auth.UserContext
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:41
 */
@RestController
@RequestMapping("/agent/workspace")
class PortalWorkspaceController(
    private val agentWorkspaceAppService: AgentWorkspaceAppService
){

    /**
     * 獲取工作區下的助理
     */
    @GetMapping("/agents")
    fun getAgents(): Response<List<AgentDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentWorkspaceAppService.getAgents(userId))
    }

    /**
     * 刪除工作區中的助理
     *
     * @param id 助理ID
     */
    @DeleteMapping("/agents/{id}")
    fun deleteAgent(@PathVariable id: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        agentWorkspaceAppService.deleteAgent(id, userId)
        return Response.success()
    }

    /**
     * 設定 Agent 的模型
     * @param modelId 模型ID
     * @param agentId Agent ID
     */
    @PutMapping("/{agentId}/model/{modelId}")
    fun saveModelId(@PathVariable modelId: String, @PathVariable agentId: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        agentWorkspaceAppService.saveModel(agentId, userId, modelId)
        return Response.success()
    }

    /**
     * 根據 agentId 和 userId 獲取對應的 modelId
     * @param agentId Agent ID
     */
    @GetMapping("/{agentId}/model")
    fun getConfiguredModelId(@PathVariable agentId: String): Response<Map<String, String?>> {
        val userId = UserContext.getCurrentUserId()
        val modelId = agentWorkspaceAppService.getConfiguredModelId(agentId, userId)
        // 使用 Kotlin 的 mapOf 函數更簡潔地創建 Map
        val result: Map<String, String?> = mapOf("modelId" to modelId)
        return Response.success(result)
    }



}