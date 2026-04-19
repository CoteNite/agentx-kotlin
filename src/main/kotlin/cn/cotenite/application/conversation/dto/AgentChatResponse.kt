package cn.cotenite.application.conversation.dto

import cn.cotenite.application.task.dto.TaskDTO
import cn.cotenite.domain.conversation.constant.MessageType

/**
 * @author  yhk
 * Description  
 * Date  2026/3/29 17:15
 */
data class AgentChatResponse(
    /**
     * 响应内容片段
     */
    var content: String? = null,

    /**
     * 是否是最后一个片段
     */
    var done: Boolean = false,

    /**
     * 消息类型
     */
    var messageType: MessageType = MessageType.TEXT,

    /**
     * 关联的任务ID（可选）
     */
    var taskId: String? = null,

    /**
     * 数据载荷，用于传递非文本内容
     */
    var payload: String? = null,

    /**
     * 时间戳
     */
    var timestamp: Long = System.currentTimeMillis(),

    /**
     * 任务列表，用于TASK_IDS类型消息
     */
    var tasks: List<TaskDTO>? = null
) {

    companion object {
        /**
         * 构建结束消息
         */
        fun buildEndMessage(messageType: MessageType): AgentChatResponse {
            return AgentChatResponse(
                content = "",
                done = true,
                messageType = messageType
            )
        }

        /**
         * 构建带内容的结束消息
         */
        fun buildEndMessage(content: String, messageType: MessageType): AgentChatResponse {
            return AgentChatResponse(
                content = content,
                done = true,
                messageType = messageType
            )
        }

        /**
         * 构建普通消息片段
         */
        fun build(content: String, messageType: MessageType): AgentChatResponse {
            return AgentChatResponse(
                content = content,
                done = false,
                messageType = messageType
            )
        }
    }
}