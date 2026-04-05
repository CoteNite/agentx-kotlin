package cn.cotenite.infrastructure.transport

import cn.cotenite.application.conversation.dto.AgentChatResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@Component("sse")
class SseMessageTransport:MessageTransport<SseEmitter> {

    companion object{
        /**
         * 系统超时消息
         */
        private const val TIMEOUT_MESSAGE: String = "\n\n[系统提示：响应超时，请重试]"

        /**
         * 系统错误消息前缀
         */
        private const val ERROR_MESSAGE_PREFIX: String = "\n\n[系统错误："
    }

    override fun createConnection(timeout: Long): SseEmitter = SseEmitter(timeout).apply {
        onTimeout {
            runCatching {
                val response = AgentChatResponse()
                response.content=TIMEOUT_MESSAGE
                response.done=true
                send(response)
                complete()
            }
        }
        onError { ex ->
            runCatching {
                val response = AgentChatResponse()
                response.content= ERROR_MESSAGE_PREFIX + ex.message + "]"
                response.done=true
                send(response)
                complete()
            }
        }
    }

    override fun sendMessage(
        connection: SseEmitter,
        streamChatResponse: AgentChatResponse
    ){
        try {
            connection.send(streamChatResponse)
        }catch (e: IOException){
            throw RuntimeException(e)
        }
    }

    override fun sendEndMessage(
        connection: SseEmitter,
        streamChatResponse: AgentChatResponse
    ) {
        try {
            connection.send(streamChatResponse)
        }catch (e: IOException){
            throw RuntimeException(e)
        }finally {
            connection.complete()
        }
    }


    override fun completeConnection(connection: SseEmitter) = connection.complete()

    override fun handleError(connection: SseEmitter, error: Throwable) {
        try {
            val response = AgentChatResponse()
            response.content=error.message
            response.done=true
            connection.send(response)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            connection.complete()
        }
    }


}
