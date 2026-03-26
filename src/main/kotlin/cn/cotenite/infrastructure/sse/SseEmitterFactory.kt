package cn.cotenite.infrastructure.sse

import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import cn.cotenite.application.conversation.dto.StreamChatResponse

/**
 * SSE发射器工厂
 */
@Component
class SseEmitterFactory {

    fun createEmitter(timeoutMillis: Long): SseEmitter =
        SseEmitter(timeoutMillis).apply {
            onTimeout {
                runCatching {
                    send(StreamChatResponse(content = "\n\n[系统提示：响应超时，请重试]", done = true))
                }
                complete()
            }
            onError { ex ->
                runCatching {
                    send(StreamChatResponse(content = "\n\n[系统错误：${ex.message}]", done = true))
                }
                complete()
            }
        }

    fun sendPartialResponse(emitter: SseEmitter, content: String, providerName: String?, modelId: String?) =
        emitter.send(StreamChatResponse(content = content, done = false, provider = providerName, model = modelId))

    fun sendCompleteResponse(emitter: SseEmitter, providerName: String?, modelId: String?) {
        emitter.send(StreamChatResponse(content = "", done = true, provider = providerName, model = modelId))
        emitter.complete()
    }

    fun sendErrorResponse(emitter: SseEmitter, error: String) {
        emitter.send(StreamChatResponse(content = error, done = true))
        emitter.complete()
    }
}
