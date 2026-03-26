package cn.cotenite.interfaces.api.admin

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.application.agent.dto.AgentVersionDTO
import cn.cotenite.application.agent.service.AgentAppService
import cn.cotenite.domain.agent.constant.PublishStatus
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.agent.ReviewAgentVersionRequest

/**
 * 管理员Agent管理控制器
 */
@RestController
@RequestMapping("/admin/agent")
class AdminAgentController(
    private val agentAppService: AgentAppService
) {

    /**
     * 获取版本列表，可按状态筛选
     */
    @GetMapping("/versions")
    fun getVersions(@RequestParam(required = false) status: Int?): Result<List<AgentVersionDTO>> =
        Result.success(agentAppService.getVersionsByStatus(PublishStatus.fromCode(status)))

    /**
     * 更新版本状态
     */
    @PostMapping("/versions/{versionId}/status")
    fun updateVersionStatus(
        @PathVariable versionId: String,
        @RequestParam status: Int,
        @RequestParam(required = false) reason: String?
    ): Result<AgentVersionDTO?> {
        val publishStatus = PublishStatus.fromCode(status)

        if (publishStatus == PublishStatus.REJECTED && reason.isNullOrBlank()) {
            return Result.serverError("拒绝操作需要提供原因")
        }

        val request = ReviewAgentVersionRequest().apply {
            this.status = publishStatus
            rejectReason = reason?.takeIf { publishStatus == PublishStatus.REJECTED }
        }

        return Result.success(agentAppService.reviewAgentVersion(versionId, request))
    }
}
