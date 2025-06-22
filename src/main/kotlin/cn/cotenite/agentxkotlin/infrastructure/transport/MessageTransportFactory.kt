package cn.cotenite.agentxkotlin.infrastructure.transport

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:25
 */
@Component
class MessageTransportFactory{

    companion object{
        const val TRANSPORT_TYPE_SSE: String = "sse"

        const val TRANSPORT_TYPE_WEBSOCKET: String = "websocket"
    }

    private val transports = HashMap<String, MessageTransport<*>>()

    @Autowired
    constructor(sseTransport: SseMessageTransport) {
        transports.put(TRANSPORT_TYPE_SSE, sseTransport)
    }

    /**
     * 获取指定类型的消息传输实现
     * @param type 传输类型
     * @return 消息传输实现
     */
    @SuppressWarnings("unchecked")
    fun <T> getTransport(type: String?): MessageTransport<T> {
        return transports.getOrDefault(type, transports[TRANSPORT_TYPE_SSE]) as MessageTransport<T>
    }


}