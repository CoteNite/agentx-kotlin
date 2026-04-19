package cn.cotenite.interfaces.api.admin

import cn.cotenite.application.admin.tool.service.AdminToolAppService
import cn.cotenite.domain.tool.constant.ToolStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.interfaces.api.common.Result

/**
 * @author  yhk
 * Description
 * Date  2026/4/6 20:41
 */
@RestController
@RequestMapping("/admin/tools")
class AdminToolController(
    private val adminToolAppService: AdminToolAppService
){

    /**
     * 修改工具的状态
     * @param toolId 工具 id
     * @param status 工具状态
     * @param reason 如果审核未通过，则说明未通过原因
     */
    @PostMapping("/{toolId}/status")
    fun updateStatus(
        @PathVariable toolId: String,
        @RequestParam status: ToolStatus,
        @RequestParam(required = false) reason: String?
    ): Result<Unit> {
        // 利用 Kotlin 的 String 扩展函数进行逻辑判断
        if (status == ToolStatus.FAILED && reason.isNullOrBlank()) {
            return Result.serverError("拒绝操作需要提供原因")
        }

        adminToolAppService.updateToolStatus(toolId, status, reason)
        return Result.success()
    }

}