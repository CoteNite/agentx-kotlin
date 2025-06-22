package cn.cotenite.agentxkotlin.infrastructure.sse

import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.function.Consumer


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:19
 */
@Component
class SseEmitterFactory {

    /**
     * 创建带有超时和错误处理的SSE发射器
     *
     * @param timeoutMillis 超时时间(毫秒)
     * @return 配置好的SSE发射器
     */
    fun createEmitter(timeoutMillis: Long): SseEmitter {
        val emitter = SseEmitter(timeoutMillis)

        emitter.onTimeout(Runnable {
            try {
                val response = StreamChatResponse(
                    content = "\n\n[系统提示：响应超时，请重试]",
                    done = true,
                )
                emitter.send(response)
                emitter.complete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })

        // 添加错误回调
        emitter.onError { ex: Throwable ->
            try {
                val response = StreamChatResponse(
                    content = "\n\n[系统错误：" + ex.message + "]",
                    done = true
                )
                emitter.send(response)
                emitter.complete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return emitter
    }

    /**
     * 发送部分响应
     *
     * @param emitter      SSE发射器
     * @param content      响应内容
     * @param providerName 提供商名称
     * @param modelId      模型ID
     */
    fun sendPartialResponse(
        emitter: SseEmitter,
        content: String,
        providerName: String,
        modelId: String
    ) {
        try {
            val response = StreamChatResponse(
                content = content,
                done = false,
                provider = providerName,
                model = modelId
            )
            emitter.send(response)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 发送完成响应
     *
     * @param emitter      SSE发射器
     * @param providerName 提供商名称
     * @param modelId      模型ID
     */
    fun sendCompleteResponse(
        emitter: SseEmitter,
        providerName: String?,
        modelId: String?
    ) {
        try {
            val response = StreamChatResponse(
                content = "",
                done = true,
                provider = providerName,
                model = modelId
            )
            emitter.send(response)
            emitter.complete()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 发送错误响应
     *
     * @param emitter SSE发射器
     * @param error   错误信息
     */
    fun sendErrorResponse(emitter: SseEmitter, error: String) {
        try {
            val response = StreamChatResponse(
                content = error,
                done = true
            )
            emitter.send(response)
            emitter.complete()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

}