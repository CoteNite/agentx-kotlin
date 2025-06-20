package cn.cotenite.agentxkotlin.interfaces.api.portal.conversation

import cn.cotenite.agentxkotlin.application.agent.service.AgentWorkspaceAppService
import cn.cotenite.agentxkotlin.domain.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.infrastructure.auth.UserContext
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import org.springframework.web.bind.annotation.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 02:33
 */
@RestController
@RequestMapping("/agent/workspace")
class PortalWorkspaceController(
    private val agentWorkspaceAppService: AgentWorkspaceAppService
){

    /**
     * 获取工作区下的助理
     *
     * @return
     */
    @GetMapping("/agents")
    fun getAgents(): Response<MutableList<AgentDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentWorkspaceAppService.getAgents(userId))
    }


    /**
     * 删除工作区中的助理
     *
     * @param id 助理id
     */
    @DeleteMapping("/agents/{id}")
    fun deleteAgent(@PathVariable id: String): Response<Unit> {
        val userId: String? = UserContext.getCurrentUserId()
        agentWorkspaceAppService.deleteAgent(id, userId)
        return Response.success()
    }

}