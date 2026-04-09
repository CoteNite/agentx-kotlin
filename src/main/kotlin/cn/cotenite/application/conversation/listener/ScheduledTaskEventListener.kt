package cn.cotenite.application.conversation.listener

import cn.cotenite.application.conversation.dto.ChatRequest
import cn.cotenite.application.conversation.service.ConversationAppService
import cn.cotenite.domain.scheduledtask.event.ScheduledTaskExecuteEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/** 定时任务事件监听器 监听Domain层发布的任务执行事件，调用ConversationAppService执行对话 */
@Component
class ScheduledTaskEventListener(
    private val conversationAppService: ConversationAppService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /** 与 DelayedTaskQueueManager 对齐，使用同一类型的 IO 调度器协程作用域 */
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** 处理定时任务执行事件 */
    @EventListener
    fun handleTaskExecuteEvent(event: ScheduledTaskExecuteEvent) {
        scope.launch {
            runCatching {
                logger.info(
                    "接收到定时任务执行事件: taskId={}, userId={}, sessionId={}",
                    event.taskId, event.userId, event.sessionId
                )

                val chatRequest = ChatRequest(
                    message = event.content,
                    sessionId = event.sessionId
                )

                conversationAppService.chat(chatRequest, event.userId)

                logger.info("定时任务消息发送成功: taskId={}", event.taskId)
            }.onFailure { e ->
                logger.error("处理定时任务执行事件失败: taskId={}, error={}", event.taskId, e.message, e)
            }
        }
    }
}
