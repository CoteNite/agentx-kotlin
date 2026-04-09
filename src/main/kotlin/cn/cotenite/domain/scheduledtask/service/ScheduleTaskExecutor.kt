package cn.cotenite.domain.scheduledtask.service

import cn.cotenite.domain.scheduledtask.constant.RepeatType
import cn.cotenite.domain.scheduledtask.event.ScheduledTaskExecuteEvent
import cn.cotenite.domain.scheduledtask.model.ScheduledTaskEntity
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/** 定时任务执行器 负责实际执行定时任务，通过事件发布与 Application 层解耦 */
@Service
class ScheduleTaskExecutor(
    private val eventPublisher: ApplicationEventPublisher,
    private val scheduledTaskDomainService: ScheduledTaskDomainService,
    private val taskScheduleService: TaskScheduleService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 执行定时任务
     * @param task 定时任务实体
     */
    fun executeTask(task: ScheduledTaskEntity) {
        runCatching {
            logger.info("开始执行定时任务: taskId={}, content={}", task.id, task.content)

            if (!task.isActive()) {
                logger.warn("任务状态不是ACTIVE，跳过执行: taskId={}, status={}", task.id, task.status)
                return
            }

            // 发布任务执行事件，由 Application 层监听并处理实际的对话逻辑
            val event = ScheduledTaskExecuteEvent(
                source    = this,
                taskId    = task.id ?: return,
                userId    = task.userId ?: return,
                sessionId = task.sessionId ?: return,
                content   = task.content ?: return
            )
            eventPublisher.publishEvent(event)
            logger.info("定时任务执行事件已发布: taskId={}", task.id)

            // 记录执行时间并处理下次执行
            handleTaskExecution(task)
        }.onFailure { e ->
            logger.error("定时任务执行异常: taskId={}, error={}", task.id, e.message, e)
        }
    }

    /**
     * 检查任务是否可以执行
     * @param task 任务实体
     * @return 是否可以执行
     */
    fun canExecute(task: ScheduledTaskEntity?): Boolean {
        if (task == null) return false

        if (!task.isActive()) {
            logger.debug("任务状态不是ACTIVE: taskId={}, status={}", task.id, task.status)
            return false
        }

        val now = LocalDateTime.now()
        if (!taskScheduleService.shouldExecuteAt(task, now)) {
            logger.debug("任务当前时间不应该执行: taskId={}, currentTime={}", task.id, now)
            return false
        }

        return true
    }

    // ── 私有方法 ──────────────────────────────────────────────────────────────

    /** 处理任务执行后的逻辑 */
    private fun handleTaskExecution(task: ScheduledTaskEntity) {
        runCatching {
            val now = LocalDateTime.now()

            // 记录执行时间
            task.recordExecution()
            scheduledTaskDomainService.recordExecution(task.id!!, now)

            // 计算并处理下次执行时间
            val nextExecuteTime = taskScheduleService.calculateNextExecuteTime(task, now)

            if (nextExecuteTime != null) {
                task.nextExecuteTime = nextExecuteTime
                scheduledTaskDomainService.updateTask(task)
                logger.info("任务下次执行时间已更新: taskId={}, nextTime={}", task.id, nextExecuteTime)
            } else {
                // 任务已完成（一次性任务或重复任务到达截止时间）
                task.complete()
                scheduledTaskDomainService.completeTask(task.id!!, task.userId!!)

                if (task.repeatType == RepeatType.NONE) {
                    logger.info("一次性任务执行完成: taskId={}", task.id)
                } else {
                    logger.info("重复任务已到截止时间，执行完成: taskId={}", task.id)
                }
            }
        }.onFailure { e ->
            logger.error("处理任务执行后逻辑失败: taskId={}, error={}", task.id, e.message, e)
        }
    }
}
