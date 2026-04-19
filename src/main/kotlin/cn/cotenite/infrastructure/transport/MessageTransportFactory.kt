package cn.cotenite.infrastructure.transport

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@Component
class MessageTransportFactory(
    private val transports: Map<String, MessageTransport<*>>
) {
    companion object {
        const val TRANSPORT_TYPE_SSE = "sse"
        const val TRANSPORT_TYPE_WEBSOCKET = "websocket"
        const val TRANSPORT_TYPE_SYNC="sync"
    }



    @Suppress("UNCHECKED_CAST")
    fun <T> getTransport(type: String): MessageTransport<T> =
        (transports[type] ?: transports[TRANSPORT_TYPE_SSE]) as MessageTransport<T>
}