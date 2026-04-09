package cn.cotenite.domain.scheduledtask.service

import cn.cotenite.domain.scheduledtask.model.ScheduledTaskEntity
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/** 定时任务执行服务 协调整个定时任务执行流程 */
@Service
class ScheduledTaskExecutionService(
    private val scheduledTaskDomainService: ScheduledTaskDomainService,
    private val taskScheduleService: TaskScheduleService,
    private val queueManager: DelayedTaskQueueManager
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /** 初始化服务，加载现有的活跃任务到延迟队列 */
    @PostConstruct
    fun init() {
        loadActiveTasksToQueue()
        logger.info("定时任务执行服务已启动，活跃任务已加载到延迟队列")
    }

    /** 调度新创建的任务 */
    fun scheduleTask(task: ScheduledTaskEntity) {
        if (!task.isActive()) return

        val now = LocalDateTime.now()
        var nextExecuteTime = task.nextExecuteTime

        if (nextExecuteTime == null) {
            nextExecuteTime = taskScheduleService.calculateNextExecuteTime(task, now)
            if (nextExecuteTime != null) {
                task.nextExecuteTime = nextExecuteTime
                scheduledTaskDomainService.updateTask(task)
            }
        }

        if (nextExecuteTime != null && nextExecuteTime.isAfter(now)) {
            queueManager.addTask(task, nextExecuteTime)
            logger.info("任务已调度: taskId={}, nextExecuteTime={}", task.id, nextExecuteTime)
        }
    }

    /** 取消任务调度 */
    fun cancelTask(taskId: String) {
        queueManager.removeTask(taskId)
        logger.info("任务调度已取消: taskId={}", taskId)
    }

    /** 批量取消任务调度 */
    fun cancelTasks(taskIds: List<String>) {
        taskIds.forEach { cancelTask(it) }
    }

    /** 重新调度任务（用于任务更新后） */
    fun rescheduleTask(task: ScheduledTaskEntity) {
        cancelTask(task.id!!)
        scheduleTask(task)
        logger.info("任务已重新调度: taskId={}", task.id)
    }

    /** 暂停任务 */
    fun pauseTask(taskId: String, userId: String) {
        scheduledTaskDomainService.pauseTask(taskId, userId)
        cancelTask(taskId)
        logger.info("任务已暂停: taskId={}, userId={}", taskId, userId)
    }

    /** 恢复任务 */
    fun resumeTask(taskId: String, userId: String) {
        scheduledTaskDomainService.resumeTask(taskId, userId)
        val task = scheduledTaskDomainService.getTask(taskId, userId)
        scheduleTask(task)
        logger.info("任务已恢复: taskId={}, userId={}", taskId, userId)
    }

    /** 删除任务 */
    fun deleteTask(taskId: String, userId: String) {
        cancelTask(taskId)
        scheduledTaskDomainService.deleteTask(taskId, userId)
        logger.info("任务已删除: taskId={}, userId={}", taskId, userId)
    }

    /** 批量删除指定会话的所有定时任务 */
    fun deleteTasksBySessionId(sessionId: String, userId: String) {
        scheduledTaskDomainService.getTasksBySessionId(sessionId)
            .filter { it.userId == userId }
            .forEach { cancelTask(it.id!!) }

        val deletedCount = scheduledTaskDomainService.deleteTasksBySessionId(sessionId, userId)
        logger.info("已删除会话 {} 的 {} 个定时任务, userId={}", sessionId, deletedCount, userId)
    }

    /** 批量删除指定 Agent 的所有定时任务 */
    fun deleteTasksByAgentId(agentId: String, userId: String) {
        scheduledTaskDomainService.getTasksByAgentId(agentId)
            .filter { it.userId == userId }
            .forEach { cancelTask(it.id!!) }

        val deletedCount = scheduledTaskDomainService.deleteTasksByAgentId(agentId, userId)
        logger.info("已删除Agent {} 的 {} 个定时任务, userId={}", agentId, deletedCount, userId)
    }

    /** 获取队列状态信息 */
    fun getQueueSize(): Int = queueManager.queueSize

    // ── 私有方法 ──────────────────────────────────────────────────────────────

    /** 加载现有的活跃任务到延迟队列 */
    private fun loadActiveTasksToQueue() {
        runCatching {
            val activeTasks = scheduledTaskDomainService.getActiveTasksToExecute()
            val now = LocalDateTime.now()

            activeTasks.forEach { task ->
                var nextExecuteTime = task.nextExecuteTime

                // 如果没有下次执行时间，计算一个
                if (nextExecuteTime == null) {
                    nextExecuteTime = taskScheduleService.calculateNextExecuteTime(task, now)
                    if (nextExecuteTime != null) {
                        task.nextExecuteTime = nextExecuteTime
                        scheduledTaskDomainService.updateTask(task)
                    }
                }

                when {
                    // 未来的任务正常调度
                    nextExecuteTime != null && nextExecuteTime.isAfter(now) ->
                        queueManager.addTask(task, nextExecuteTime)
                    // 过期任务，立即执行一次
                    nextExecuteTime != null && nextExecuteTime.isBefore(now) ->
                        queueManager.addTask(task, now.plusSeconds(1))
                }
            }

            logger.info("已加载 {} 个活跃任务到延迟队列", activeTasks.size)
        }.onFailure { e ->
            logger.error("加载活跃任务到延迟队列失败: {}", e.message, e)
        }
    }
}
