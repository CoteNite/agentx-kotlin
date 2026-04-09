package cn.cotenite.interfaces.dto.scheduledtask.request

import cn.cotenite.domain.scheduledtask.constant.RepeatType
import cn.cotenite.domain.scheduledtask.model.RepeatConfig
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/** 创建定时任务请求 */
class CreateScheduledTaskRequest {

    @NotBlank(message = "Agent ID不能为空")
    var agentId: String? = null

    @NotBlank(message = "会话ID不能为空")
    var sessionId: String? = null

    @NotBlank(message = "任务内容不能为空")
    var content: String? = null

    @NotNull(message = "重复类型不能为空")
    var repeatType: RepeatType? = null

    @NotNull(message = "重复配置不能为空")
    var repeatConfig: RepeatConfig? = null
}
