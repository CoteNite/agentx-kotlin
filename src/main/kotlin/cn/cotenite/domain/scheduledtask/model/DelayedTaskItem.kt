package cn.cotenite.domain.scheduledtask.model

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

/**
 * 延迟队列任务项
 * 用于延迟队列中的定时任务执行
 *
 * @author yhk
 * @since 2026/4/9 18:10
 */
class DelayedTaskItem(
    val task: ScheduledTaskEntity,
    executeTime: LocalDateTime
) : Delayed {

    /** 任务ID */
    val taskId: String = task.id ?: throw IllegalArgumentException("Task ID cannot be null")

    /** 执行时间(毫秒时间戳) */
    private val executeTime: Long = executeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    override fun getDelay(unit: TimeUnit): Long {
        val delay = executeTime - System.currentTimeMillis()
        return unit.convert(delay, TimeUnit.MILLISECONDS)
    }

    override fun compareTo(other: Delayed): Int {
        if (this === other) return 0

        return if (other is DelayedTaskItem) {
            this.executeTime.compareTo(other.executeTime)
        } else {
            this.getDelay(TimeUnit.MILLISECONDS).compareTo(other.getDelay(TimeUnit.MILLISECONDS))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DelayedTaskItem
        return taskId == other.taskId
    }

    override fun hashCode(): Int = taskId.hashCode()

    override fun toString(): String {
        return "DelayedTaskItem(taskId='$taskId', executeTime=$executeTime)"
    }
}