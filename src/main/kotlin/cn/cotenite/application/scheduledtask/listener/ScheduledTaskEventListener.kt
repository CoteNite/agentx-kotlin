package cn.cotenite.application.scheduledtask.listener

import cn.cotenite.application.conversation.dto.ChatRequest
import cn.cotenite.application.conversation.service.ConversationAppService
import cn.cotenite.domain.scheduledtask.event.ScheduledTaskExecuteEvent
import cn.cotenite.domain.scheduledtask.service.ScheduledTaskDomainService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/** 定时任务事件监听器 监听Domain层发布的任务执行事件，调用ConversationAppService执行对话 */
@Component
class ScheduledTaskEventListener(
    private val conversationAppService: ConversationAppService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /** 处理定时任务执行事件 */
    @EventListener
    @Async
    fun handleTaskExecuteEvent(event: ScheduledTaskExecuteEvent) {
        try {
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

            // 直接调用Domain服务记录执行成功（如果需要的话）
            // scheduledTaskDomainService.recordExecutionSuccess(event.taskId)

        } catch (e: Exception) {
            logger.error("处理定时任务执行事件失败: taskId={}, error={}", event.taskId, e.message, e)

            // 直接调用Domain服务记录执行失败（如果需要的话）
            // scheduledTaskDomainService.recordExecutionFailure(event.taskId, e.message)
        }
    }
}
