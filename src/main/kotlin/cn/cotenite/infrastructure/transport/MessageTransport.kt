package cn.cotenite.infrastructure.transport

import cn.cotenite.application.conversation.dto.AgentChatResponse

/**
 * 消息传输接口 - 支持 coroutines 和 Flow
 */
interface MessageTransport<T> {
    fun createConnection(timeout: Long): T

    /**
     * 发送消息
     * @param connection 连接对象
     * @param streamChatResponse 消息内容
     */
    fun sendMessage(connection: T, streamChatResponse: AgentChatResponse)

    /**
     * 发送消息
     * @param connection 连接对象
     * @param streamChatResponse 消息内容
     */
    fun sendEndMessage(connection: T, streamChatResponse: AgentChatResponse)

    fun completeConnection(connection: T)

    fun handleError(connection: T, error: Throwable)


}
