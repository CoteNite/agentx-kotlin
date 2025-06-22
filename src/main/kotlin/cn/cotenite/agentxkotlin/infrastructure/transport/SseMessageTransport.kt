package cn.cotenite.agentxkotlin.infrastructure.transport

import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.function.Consumer


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:27
 */
@Component
class SseMessageTransport: MessageTransport<SseEmitter>{

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

    override fun createConnection(timeout: Long): SseEmitter {
        val emitter = SseEmitter(timeout)


        // 添加超时回调
        emitter.onTimeout {
            try {
                val response = StreamChatResponse(
                    content = TIMEOUT_MESSAGE,
                    done = true,
                )
                emitter.send(response)
                emitter.complete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }


        // 添加错误回调
        emitter.onError { ex: Throwable ->
            try {
                val response = StreamChatResponse(
                    content = ERROR_MESSAGE_PREFIX + ex.message + "]",
                    done = true,
                )
                emitter.send(response)
                emitter.complete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return emitter
    }

    override fun sendMessage(
        connection: SseEmitter,
        content: String,
        isDone: Boolean,
        provider: String,
        model: String
    ) {
        try {
            val response = StreamChatResponse(
                content = content,
                done = isDone,
                provider=provider,
                model = model
            )
            connection.send(response)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun completeConnection(connection: SseEmitter) {
        connection.complete()
    }

    override fun handleError(
        connection: SseEmitter,
        error: Throwable
    ) {
        try {
            val response = StreamChatResponse(
                content = error.message?:"",
                done = true
            )
            connection.send(response)
            connection.complete()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}