package cn.cotenite.interfaces.dto.scheduledtask.request

import cn.cotenite.domain.scheduledtask.constant.RepeatType
import cn.cotenite.domain.scheduledtask.constant.ScheduleTaskStatus
import cn.cotenite.domain.scheduledtask.model.RepeatConfig
import jakarta.validation.constraints.NotBlank

/** 更新定时任务请求 */
class UpdateScheduledTaskRequest {

    @NotBlank(message = "任务ID不能为空")
    var id: String? = null

    var content: String? = null

    var repeatType: RepeatType? = null

    var repeatConfig: RepeatConfig? = null

    var status: ScheduleTaskStatus? = null
}
