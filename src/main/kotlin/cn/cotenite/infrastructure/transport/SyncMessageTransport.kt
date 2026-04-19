package cn.cotenite.infrastructure.transport

import cn.cotenite.application.conversation.dto.AgentChatResponse
import cn.cotenite.application.conversation.dto.ChatResponse
import org.springframework.stereotype.Component

/** 同步消息传输实现 */
@Component("sync")
class SyncMessageTransport: MessageTransport<ChatResponse>{
    override fun createConnection(timeout: Long): ChatResponse {
        return ChatResponse()
    }

    override fun sendMessage(
        connection: ChatResponse,
        streamChatResponse: AgentChatResponse
    ) {
        // 同步模式下，累积消息内容
        val existingContent: String? = if (connection.content != null) connection.content else ""
        connection.content=existingContent + streamChatResponse.content
        connection.timestamp= streamChatResponse.timestamp
    }

    override fun sendEndMessage(
        connection: ChatResponse,
        streamChatResponse: AgentChatResponse
    ) {


        // 同步模式下，设置最终响应
        if (streamChatResponse.content != null && !streamChatResponse.content!!.isEmpty()) {
            val existingContent: String? = if (connection.content != null) connection.content else ""
            connection.content=existingContent + streamChatResponse.content
        }
        connection.timestamp=streamChatResponse.timestamp
    }

    override fun completeConnection(connection: ChatResponse) {

    }

    override fun handleError(
        connection: ChatResponse,
        error: Throwable
    ) {
        connection.content="错误: " + error.message
        connection.timestamp=System.currentTimeMillis()
    }
}