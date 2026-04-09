package cn.cotenite.domain.scheduledtask.event

import org.springframework.context.ApplicationEvent

/**
 * 定时任务执行事件
 * 当定时任务需要执行时，Domain层发布此事件
 */
class ScheduledTaskExecuteEvent(
    source: Any,
    val taskId: String,
    val userId: String,
    val sessionId: String,
    val content: String
) : ApplicationEvent(source) {

    override fun toString(): String {
        return "ScheduledTaskExecuteEvent{taskId='$taskId', userId='$userId', sessionId='$sessionId', content='$content'}"
    }
}
