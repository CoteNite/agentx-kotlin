package cn.cotenite.agentxkotlin.interfaces.api.admin

import cn.cotenite.agentxkotlin.application.agent.dto.AgentVersionDTO
import cn.cotenite.agentxkotlin.application.agent.service.AgentAppService
import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import cn.cotenite.agentxkotlin.interfaces.dto.agent.ReviewAgentVersionRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * @Author  RichardYoung
 * @Description  
 * @Date  2025/6/22 18:27
*/
@RestController // 標註為 REST 控制器
@RequestMapping("/admin/agent") // 設定基礎請求路徑
class AdminAgentController(
    private val agentAppService: AgentAppService
){

    /**
     * 獲取版本列表，可按狀態篩選
     *
     * @param status 版本狀態（可選）：REVIEWING - 審核中，PUBLISHED - 已發布，REJECTED - 已拒絕，REMOVED - 已下架
     * @return 符合條件的版本列表（每個助理只返回最新版本）
     */
    @GetMapping("/versions")
    fun getVersions(@RequestParam(required = false) status: Int?): Response<List<AgentVersionDTO>> {
        val publishStatus = status?.let { PublishStatus.fromCode(it) }
        return Response.success(agentAppService.getVersionsByStatus(publishStatus))
    }

    /**
     * 更新版本狀態（包括審核通過/拒絕/下架等操作）
     *
     * @param versionId 版本ID
     * @param status 目標狀態: PUBLISHED, REJECTED, REMOVED
     * @param reason 原因（拒絕時需要提供）
     * @return 更新後的版本
     */
    @PostMapping("/versions/{versionId}/status")
    fun updateVersionStatus(
        @PathVariable versionId: String,
        @RequestParam status: Int,
        @RequestParam(required = false) reason: String?
    ): Response<out AgentVersionDTO?> {

        val publishStatus = PublishStatus.fromCode(status)

        if (publishStatus == PublishStatus.REJECTED && reason.isNullOrEmpty()) {
            return Response.serverError("拒絕操作需要提供原因")
        }
        val request = ReviewAgentVersionRequest(status = publishStatus) // 直接在構造函數中設置屬性

        // 只有在拒絕時設置原因
        if (publishStatus == PublishStatus.REJECTED) {
            request.rejectReason = reason // 直接訪問屬性
        }

        return Response.success(agentAppService.reviewAgentVersion(versionId, request))
    }

}