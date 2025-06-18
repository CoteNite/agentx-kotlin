package cn.cotenite.agentxkotlin.interfaces.api.admin

import cn.cotenite.agentxkotlin.application.agent.service.AgentAppService
import cn.cotenite.agentxkotlin.domain.agent.model.AgentVersionDTO
import cn.cotenite.agentxkotlin.domain.agent.model.PublishStatus
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import cn.cotenite.agentxkotlin.interfaces.dto.agent.ReviewAgentVersionRequest
import org.springframework.web.bind.annotation.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/19 00:27
 */
@RestController
@RequestMapping("/admin/agent")
class AdminAgentController(
    private val agentAppService: AgentAppService
){

    /**
     * 获取版本列表，可按状态筛选
     *
     * @param status 版本状态（可选）：REVIEWING - 审核中，PUBLISHED - 已发布，REJECTED - 已拒绝，REMOVED - 已下架
     * @return 符合条件的版本列表（每个助理只返回最新版本）
     */
    @GetMapping("/versions")
    fun getVersions(@RequestParam(required = false) status: Int?): Result<List<AgentVersionDTO>> {
        // 根据状态参数获取对应的版本列表

        return Result.success(agentAppService.getVersionsByStatus(PublishStatus.fromCode(status!!)))
    }

    /**
     * 更新版本状态（包括审核通过/拒绝/下架等操作）
     *
     * @param versionId 版本ID
     * @param status 目标状态: PUBLISHED, REJECTED, REMOVED
     * @param reason 原因（拒绝时需要提供）
     * @return 更新后的版本
     */
    @PostMapping("/versions/{versionId}/status")
    fun updateVersionStatus(
        @PathVariable versionId: String,
        @RequestParam status: Int?,
        @RequestParam(required = false) reason: String?
    ): Response<AgentVersionDTO> {
        val publishStatus = PublishStatus.fromCode(status!!)

        // 如果是拒绝操作，需要检查原因
        if (publishStatus == PublishStatus.REJECTED && (reason == null || reason.isEmpty())) {
            return Response.serverError("拒绝操作需要提供原因")
        }

        val request = ReviewAgentVersionRequest(publishStatus)

        if (publishStatus == PublishStatus.REJECTED) {
            request.rejectReason = reason
        }

        return Response.success(agentAppService.reviewAgentVersion(versionId, request))
    }
}
