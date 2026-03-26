package cn.cotenite.infrastructure.transport

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * 消息传输工厂
 */
@Component
class MessageTransportFactory(
    private val sseTransport: SseMessageTransport
) {

    companion object {
        const val TRANSPORT_TYPE_SSE = "sse"
        const val TRANSPORT_TYPE_WEBSOCKET = "websocket"
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getTransport(type: String): MessageTransport<T> {
        return when (type.lowercase()) {
            TRANSPORT_TYPE_SSE -> sseTransport as MessageTransport<T>
            else -> sseTransport as MessageTransport<T>
        }
    }

    fun getSseTransport(): MessageTransport<SseEmitter> = sseTransport
}
