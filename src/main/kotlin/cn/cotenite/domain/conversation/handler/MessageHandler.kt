package cn.cotenite.domain.conversation.handler

import cn.cotenite.infrastructure.transport.MessageTransport

/**
 * 消息处理器
 */
interface MessageHandler {
    fun <T> handleChat(environment: ChatEnvironment, transport: MessageTransport<T>): T
}
