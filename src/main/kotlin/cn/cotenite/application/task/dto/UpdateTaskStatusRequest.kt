package cn.cotenite.application.task.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

/**
 * 更新任务状态请求
 */
data class UpdateTaskStatusRequest(
    @field:NotBlank(message = "任务状态不能为空")
    var status: String? = null,
    @field:Min(value = 0, message = "进度不能小于0")
    @field:Max(value = 100, message = "进度不能大于100")
    var progress: Int? = null
)
