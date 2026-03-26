package cn.cotenite.infrastructure.transport

import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply
import kotlinx.coroutines.flow.Flow
import cn.cotenite.domain.conversation.handler.ChatEnvironment
import cn.cotenite.domain.conversation.model.MessageEntity

/**
 * 消息传输接口 - 支持 coroutines 和 Flow
 */
interface MessageTransport<T> {
    fun createConnection(timeout: Long): T

    fun sendMessage(connection: T, content: String, isDone: Boolean, provider: String?, model: String?)

    fun completeConnection(connection: T)

    fun handleError(connection: T, error: Throwable)

    /**
     * 使用 coroutines 消费 Flow 并向连接推送数据
     */
    fun handleStreamingResponse(
        connection: T,
        flow: Flow<StreamingChatModelReply>,
        environment: ChatEnvironment,
        userMessageEntity: MessageEntity,
        llmMessageEntity: MessageEntity
    )
}
