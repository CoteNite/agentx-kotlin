package cn.cotenite.domain.scheduledtask.model

import cn.cotenite.domain.scheduledtask.constant.RepeatType
import cn.cotenite.domain.scheduledtask.constant.ScheduleTaskStatus
import cn.cotenite.infrastructure.converter.RepeatConfigConverter
import cn.cotenite.infrastructure.converter.RepeatTypeConverter
import cn.cotenite.infrastructure.converter.ScheduledTaskStatusConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime
import java.util.Objects

/** 定时任务实体类 代表一个用户创建的定时任务 */
@TableName(value = "scheduled_tasks", autoResultMap = true)
class ScheduledTaskEntity : BaseEntity() {

    /** 定时任务唯一ID */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null

    /** 用户ID */
    @TableField("user_id")
    var userId: String? = null

    /** 关联的Agent ID */
    @TableField("agent_id")
    var agentId: String? = null

    /** 关联的会话ID */
    @TableField("session_id")
    var sessionId: String? = null

    /** 任务内容 */
    @TableField("content")
    var content: String? = null

    /** 重复类型 */
    @TableField(value = "repeat_type", typeHandler = RepeatTypeConverter::class)
    var repeatType: RepeatType? = null

    /** 重复配置，JSON格式 */
    @TableField(value = "repeat_config", typeHandler = RepeatConfigConverter::class)
    var repeatConfig: RepeatConfig? = null

    /** 任务状态 */
    @TableField(value = "status", typeHandler = ScheduledTaskStatusConverter::class)
    var status: ScheduleTaskStatus? = null

    /** 上次执行时间 */
    @TableField("last_execute_time")
    var lastExecuteTime: LocalDateTime? = null

    /** 下次执行时间 */
    @TableField("next_execute_time")
    var nextExecuteTime: LocalDateTime? = null

    /** 伴生对象，用于静态工厂方法 */
    companion object {
        /** 创建新的定时任务 */
        @JvmStatic
        fun createNew(
            userId: String,
            agentId: String,
            sessionId: String,
            content: String,
            repeatType: RepeatType,
            repeatConfig: RepeatConfig
        ): ScheduledTaskEntity {
            return ScheduledTaskEntity().apply {
                this.userId = userId
                this.agentId = agentId
                this.sessionId = sessionId
                this.content = content
                this.repeatType = repeatType
                this.repeatConfig = repeatConfig
                this.status = ScheduleTaskStatus.ACTIVE
            }
        }
    }

    /** 更新任务内容 */
    fun updateContent(content: String) {
        this.content = content
    }

    /** 更新重复配置 */
    fun updateRepeatConfig(repeatType: RepeatType, repeatConfig: RepeatConfig) {
        this.repeatType = repeatType
        this.repeatConfig = repeatConfig
    }

    /** 暂停任务 */
    fun pause() {
        this.status = ScheduleTaskStatus.PAUSED
    }

    /** 恢复任务 */
    fun resume() {
        this.status = ScheduleTaskStatus.ACTIVE
    }

    /** 完成任务 */
    fun complete() {
        this.status = ScheduleTaskStatus.COMPLETED
    }

    /** 记录执行时间 */
    fun recordExecution() {
        this.lastExecuteTime = LocalDateTime.now()
    }

    /** 检查任务是否活跃 */
    fun isActive(): Boolean = ScheduleTaskStatus.ACTIVE == this.status

    /** 检查任务是否暂停 */
    fun isPaused(): Boolean = ScheduleTaskStatus.PAUSED == this.status

    /** 检查任务是否完成 */
    fun isCompleted(): Boolean = ScheduleTaskStatus.COMPLETED == this.status

    /** 检查是否为一次性任务 */
    fun isOneTime(): Boolean = RepeatType.NONE == this.repeatType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScheduledTaskEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0

    override fun toString(): String {
        return "ScheduledTaskEntity(id=$id, userId=$userId, agentId=$agentId, sessionId=$sessionId, content=$content, repeatType=$repeatType, status=$status, lastExecuteTime=$lastExecuteTime, nextExecuteTime=$nextExecuteTime)"
    }
}