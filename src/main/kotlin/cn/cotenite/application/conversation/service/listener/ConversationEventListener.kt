package cn.cotenite.application.conversation.service.listener

import cn.cotenite.application.conversation.dto.ChatRequest
import cn.cotenite.application.conversation.service.ConversationAppService
import cn.cotenite.domain.scheduledtask.event.ScheduledTaskExecuteEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/** 定时任务事件监听器 监听Domain层发布的任务执行事件，调用ConversationAppService执行对话 */
@Component
class ConversationEventListener(
    private val conversationAppService: ConversationAppService
) {

    private val logger: Logger = LoggerFactory.getLogger(ConversationEventListener::class.java)

    /** 处理定时任务执行事件 */
    @EventListener
    @Async
    fun chatEvent(event: ScheduledTaskExecuteEvent) {
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

        } catch (e: Exception) {
            logger.error("处理定时任务执行事件失败: taskId={}, error={}", event.taskId, e.message, e)
        }
    }
}
