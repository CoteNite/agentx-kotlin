package cn.cotenite.domain.scheduledtask.service

import cn.cotenite.domain.scheduledtask.model.DelayedTaskItem
import cn.cotenite.domain.scheduledtask.model.ScheduledTaskEntity
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.DelayQueue

/** 延迟队列管理器 — 使用 Kotlin 协程驱动任务调度 */
@Service
class DelayedTaskQueueManager(
    private val taskExecutor: ScheduleTaskExecutor
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val delayQueue = DelayQueue<DelayedTaskItem>()

    /** 专用调度器：IO 线程池承载阻塞的 DelayQueue.take() */
    private val scope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() + CoroutineName("delayed-task-manager")
    )

    /** 任务执行通道，解耦消费与执行 */
    private val taskChannel = Channel<DelayedTaskItem>(capacity = Channel.UNLIMITED)

    @PostConstruct
    fun init() {
        launchConsumer()
        launchExecutors(poolSize = 5)
        logger.info("延迟队列管理器已启动，执行协程数: 5")
    }

    @PreDestroy
    fun destroy() {
        taskChannel.close()
        scope.cancel()
        logger.info("延迟队列管理器已关闭")
    }

    /** 添加任务到延迟队列 */
    fun addTask(task: ScheduledTaskEntity, executeTime: LocalDateTime) {
        delayQueue.offer(DelayedTaskItem(task, executeTime))
        logger.info("任务已添加到延迟队列: taskId={}, executeTime={}", task.id, executeTime)
    }

    /** 从延迟队列移除任务 */
    fun removeTask(taskId: String) {
        val removed = delayQueue.removeIf { it.taskId == taskId }
        if (removed) logger.info("任务已从延迟队列移除: taskId={}", taskId)
        else logger.debug("任务不在延迟队列中: taskId={}", taskId)
    }

    /** 获取当前队列大小 */
    val queueSize: Int get() = delayQueue.size

    /** 单个消费协程：阻塞 take()，到期后投递到 Channel */
    private fun launchConsumer() = scope.launch {
        logger.info("延迟队列消费协程已启动")
        while (isActive) {
            runCatching {
                // DelayQueue.take() 是阻塞调用，放在 IO 调度器上执行
                val item = withContext(Dispatchers.IO) { delayQueue.take() }
                logger.debug("从延迟队列取出到期任务: taskId={}", item.taskId)
                taskChannel.send(item)
            }.onFailure { e ->
                when (e) {
                    is CancellationException -> return@launch
                    else -> {
                        logger.error("队列消费异常: {}", e.message, e)
                        delay(1_000)
                    }
                }
            }
        }
        logger.info("延迟队列消费协程已停止")
    }

    /** 多个执行协程：并发消费 Channel 中的任务 */
    private fun launchExecutors(poolSize: Int) {
        repeat(poolSize) { index ->
            scope.launch(CoroutineName("task-executor-$index")) {
                for (item in taskChannel) {
                    runCatching { handleItem(item) }
                        .onFailure { e ->
                            logger.error("执行任务异常: taskId={}, error={}", item.taskId, e.message, e)
                        }
                }
            }
        }
    }

    /** 处理单个到期任务：检查是否可执行 → 执行 → 重新调度 */
    private fun handleItem(item: DelayedTaskItem) {
        val task = item.task

        if (taskExecutor.canExecute(task)) {
            taskExecutor.executeTask(task)
        } else {
            logger.info("任务不满足执行条件，跳过执行: taskId={}", task.id)
        }
    }


}
