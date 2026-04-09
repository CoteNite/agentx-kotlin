package cn.cotenite.application.scheduledtask.dto

import cn.cotenite.domain.scheduledtask.constant.RepeatType
import cn.cotenite.domain.scheduledtask.constant.ScheduleTaskStatus
import cn.cotenite.domain.scheduledtask.model.RepeatConfig
import java.time.LocalDateTime

/** 定时任务DTO */
class ScheduledTaskDTO {
    var id: String? = null
    var userId: String? = null
    var agentId: String? = null
    var sessionId: String? = null
    var content: String? = null
    var repeatType: RepeatType? = null
    var repeatConfig: RepeatConfig? = null
    var status: ScheduleTaskStatus? = null
    var lastExecuteTime: LocalDateTime? = null
    var nextExecuteTime: LocalDateTime? = null
    var createdAt: LocalDateTime? = null
    var updatedAt: LocalDateTime? = null
}
