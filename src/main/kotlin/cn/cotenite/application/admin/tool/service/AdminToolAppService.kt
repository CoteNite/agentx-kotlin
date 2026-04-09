package cn.cotenite.application.admin.tool.service

import cn.cotenite.application.tool.service.ToolAppService
import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.service.ToolDomainService
import cn.cotenite.domain.tool.service.ToolStateDomainService
import org.springframework.stereotype.Service

/**
 * @author  yhk
 * Description
 * Date  2026/4/6 20:42
 */
@Service
class AdminToolAppService(
    private val toolDomainService: ToolDomainService,
    private val toolStateDomainService: ToolStateDomainService,
    private val toolAppService: ToolAppService
){

    /**
     * 该接口用于管理员修改状态，如果当前工具是人工审核则需要
     *
     * @param toolId 工具 id
     * @param status 状态
     * @param rejectReason 拒绝原因
     */
    fun updateToolStatus(toolId: String, status: ToolStatus, rejectReason: String?) {

        val tool = toolDomainService.getTool(toolId)

        when {
            // 场景 1：人工审核通过
            tool.status == ToolStatus.MANUAL_REVIEW && status == ToolStatus.APPROVED -> {
                val approvedToolId = toolStateDomainService.manualReviewComplete(tool, true)
                // 审核通过后，手动触发自动安装
                toolAppService.autoInstallApprovedTool(approvedToolId)
            }

            // 场景 2：审核失败处理
            status == ToolStatus.FAILED -> {
                tool.failedStepStatus = tool.status
                toolDomainService.updateFailedToolStatus(tool.id!!, tool.status!!, rejectReason)
            }

            // 场景 3：非人工审核状态下变更为 APPROVED
            status == ToolStatus.APPROVED -> {
                toolDomainService.updateApprovedToolStatus(tool.id!!, status)
                toolAppService.autoInstallApprovedTool(toolId)
            }

            // 场景 4：其他状态变更
            else -> {
                toolDomainService.updateApprovedToolStatus(tool.id!!, status)
            }
        }
    }
}