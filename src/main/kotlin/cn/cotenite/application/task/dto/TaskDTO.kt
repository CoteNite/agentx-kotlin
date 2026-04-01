package cn.cotenite.application.task.dto

import java.time.LocalDateTime

/**
 * @author  yhk
 * Description  
 * Date  2026/3/29 17:19
 */
/**
 * 任务数据传输对象
 */
data class TaskDTO(
    /**
     * 任务ID
     */
    var id: String? = null,

    /**
     * 会话ID
     */
    var sessionId: String? = null,

    /**
     * 用户ID
     */
    var userId: String? = null,

    /**
     * 父任务ID
     */
    var parentTaskId: String? = null,

    /**
     * 任务名称
     */
    var taskName: String? = null,

    /**
     * 任务描述
     */
    var description: String? = null,

    /**
     * 任务状态
     */
    var status: String? = null,

    /**
     * 任务进度
     */
    var progress: Int? = null,

    /**
     * 开始时间
     */
    var startTime: LocalDateTime? = null,

    /**
     * 结束时间
     */
    var endTime: LocalDateTime? = null,

    /**
     * 创建时间
     */
    var createdAt: LocalDateTime? = null,

    /**
     * 更新时间
     */
    var updatedAt: LocalDateTime? = null
)