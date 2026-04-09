package cn.cotenite.domain.task.model

import cn.cotenite.domain.task.constant.TaskStatus
import cn.cotenite.infrastructure.converter.ScheduledTaskStatusConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

/**
 * 任务实体类
 */
@TableName("agent_tasks")
class TaskEntity : BaseEntity() {

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null

    /**
     * 所属会话ID
     */
    @TableField("session_id")
    var sessionId: String? = null

    /**
     * 用户ID
     */
    @TableField("user_id")
    var userId: String? = null

    /**
     * 父任务ID
     */
    @TableField("parent_task_id")
    var parentTaskId: String? = null

    /**
     * 任务名称
     */
    @TableField("task_name")
    var taskName: String? = null

    /**
     * 任务描述
     */
    @TableField("description")
    var description: String? = null

    /**
     * 任务状态，只有子任务有
     */
    @TableField(value = "status", typeHandler = ScheduledTaskStatusConverter::class)
    var status: TaskStatus? = null

    /**
     * 任务进度, 存放父任务中
     */
    @TableField("progress")
    var progress: Int = 0

    /**
     * 开始时间
     */
    @TableField("start_time")
    var startTime: LocalDateTime? = null

    /**
     * 结束时间
     */
    @TableField("end_time")
    var endTime: LocalDateTime? = null

    /**
     * 任务结果
     */
    @TableField("task_result")
    var taskResult: String? = null

    /**
     * 更新任务状态
     *
     * @param newStatus 新状态
     */
    fun updateStatus(newStatus: TaskStatus) {
        this.status = newStatus
        when (newStatus) {
            TaskStatus.IN_PROGRESS -> {
                if (startTime == null) startTime = LocalDateTime.now()
            }
            TaskStatus.COMPLETED -> {
                if (endTime == null) endTime = LocalDateTime.now()
                progress = 100
            }
            TaskStatus.FAILED -> {
                if (endTime == null) endTime = LocalDateTime.now()
            }
            else -> {} // 处理其他可能的状态
        }
    }

    /**
     * 更新进度
     *
     * @param value 进度值(0-100)
     */
    fun updateProgress(value: Int) {
        progress = value.coerceIn(0, 100)
    }
}