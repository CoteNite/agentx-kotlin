package cn.cotenite.agentxkotlin.interfaces.api.portal.conversation

import cn.cotenite.agentxkotlin.application.conversation.dto.ChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.ChatResponse
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import cn.cotenite.agentxkotlin.application.conversation.service.ConversationAppService
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/19 00:34
 */
@RestController
@RequestMapping("/conversation")
class PortalConversationController(
    private val conversationAppService: ConversationAppService
){

    private val logger = LoggerFactory.getLogger(PortalConversationController::class.java)

    /**
     * 普通聊天接口
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    @PostMapping("/chat")
    fun chat(@RequestBody @Validated request: ChatRequest): Response<ChatResponse> {
        logger.info("收到聊天请求: ${request.message}, 服务商: ${request.provider}, 模型: ${request.model}")

        if (request.provider.isEmpty()) {
            request.provider = "siliconflow"
        }

        try {
            val response = conversationAppService.chat(request)
            return Response.success(response)
        } catch (e: Exception) {
            logger.error("处理聊天请求异常", e)
            return Response.serverError("处理请求失败: " + e.message)
        }
    }

    /**
     * 流式聊天接口，使用SSE (Server-Sent Events) - POST方式
     *
     * @param request 流式聊天请求
     * @return SSE流式响应
     */
    @PostMapping("/chat/stream")
    fun chatStream(@RequestBody @Validated request: StreamChatRequest): SseEmitter {
        logger.info("收到流式聊天请求(POST): ${request.message}, 服务商: ${request.provider}, 模型: ${request.model}")

        if (request.provider.isEmpty()) {
            request.provider = "siliconflow"
        }

        // 创建SseEmitter，超时时间设置为5分钟
        val emitter = SseEmitter(300000L) // 5分钟超时

        // 设置超时回调
        emitter.onTimeout {
            logger.warn("流式聊天请求超时：${request.message}")
        }

        // 设置完成回调
        emitter.onCompletion {
            logger.info("流式聊天请求完成：${request.message}")
        }

        // 设置错误回调
        emitter.onError { ex: Throwable? ->
            logger.error("流式聊天请求错误", ex)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 使用新的真正流式实现
                conversationAppService.chatStream(request) { response: StreamChatResponse, isLast: Boolean ->
                    try {
                        emitter.send(response)

                        // 如果是最后一个响应块，完成请求
                        if (isLast) {
                            emitter.complete()
                        }
                    } catch (e: IOException) {
                        logger.error("发送流式响应块时出错", e)
                        emitter.completeWithError(e)
                    }
                }
            } catch (e: Exception) {
                logger.error("处理流式聊天请求发生异常", e)
                emitter.completeWithError(e)
            }
        }

        return emitter
    }

    /**
     * 流式聊天接口，使用SSE (Server-Sent Events) - GET方式
     * 为前端EventSource提供支持，因为EventSource只支持GET请求
     *
     * @param message  消息内容
     * @param provider 服务商
     * @param model    模型
     * @return SSE流式响应
     */
    @GetMapping("/chat/stream")
    fun chatStreamGet(
        @RequestParam("message") message: String,
        @RequestParam(value = "model") model: String,
        @RequestParam(value = "provider", required = false) provider: String?,
        @RequestParam(value = "sessionId", required = false) sessionId: String?
    ): SseEmitter {
        logger.info("收到流式聊天请求(GET): ${message}, 服务商: ${provider}, 模型: ${model}")

        // 创建请求对象
        val request = StreamChatRequest(
            message = message,
            provider =  provider ?: "siliconflow",
            sessionId = sessionId?:"",
            model = model,
        )

        // 调用POST方法处理
        return chatStream(request)
    }

}

