package cn.cotenite.agentxkotlin.interfaces.api.conversation

import cn.cotenite.agentxkotlin.application.conversation.dto.ChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.ChatResponse
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatRequest
import cn.cotenite.agentxkotlin.application.conversation.service.ConversationService
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
 * @Date  2025/6/15 20:51
 */
@RestController
@RequestMapping("/conversation")
class ConversationController(
    private val conversationService: ConversationService
){
    private val logger = LoggerFactory.getLogger(ConversationController::class.java)

    @PostMapping("/chat")
    fun chat(@RequestBody @Validated request: ChatRequest): Response<ChatResponse> {
        logger.info("收到聊天请求: ${request.message}, 服务商: ${request.provider}, 模型: ${request.model}")

        if (request.provider.isEmpty()){
            request.provider="siliconflow"
        }

        try {
            val response: ChatResponse = conversationService.chat(request)
            return Response.success(response)
        }catch (e:Exception){
            logger.error("处理聊天请求异常", e)
            return Response.serverError("处理请求失败: ${e.message}")
        }
    }

    @PostMapping("/chat/stream")
    fun chatStream(@RequestBody @Validated request: StreamChatRequest): SseEmitter{
        logger.info("收到流式聊天请求(POST): ${request.message}, 服务商: ${request.provider}, 模型: ${request.model}")

        if (request.provider.isEmpty()){
            request.provider="siliconflow"
        }

        val emitter = SseEmitter(300000L)

        emitter.onTimeout {
            logger.warn("流式聊天请求超时：${request.message}")
        }

        emitter.onCompletion {
            logger.info("流式聊天请求完成：${request.message}")
        }

        emitter.onError {
            logger.error("流式聊天请求出错",it)
        }

        CoroutineScope(Dispatchers.IO).launch {
            runCatching  {
                conversationService.chatStream(request) { chunk, isLast ->
                    try {
                        emitter.send(chunk)
                        if (isLast){
                            emitter.complete()
                        }
                    }catch (e:IOException){
                        logger.error("发送流式响应块时出错", e)
                        emitter.completeWithError(e)
                    }
                }
            }.onFailure{e->
                logger.error("处理流式聊天请求发生异常", e)
                emitter.completeWithError(e)
            }
        }
        return emitter
    }

    @GetMapping("/chat/stream")
    fun chatStreamGet(
        @RequestParam("message") message: String,
        @RequestParam(value = "provider", required = false) provider: String?,
        @RequestParam(value = "model", required = false) model: String?,
        @RequestParam(value = "sessionId", required = false) sessionId: String?
    ): SseEmitter? {
        logger.info("收到流式聊天请求(GET): ${message}, 服务商: ${provider}, 模型: ${model}")

        val request = StreamChatRequest(
            message = message,
            provider = provider?:"",
            model = model?:"",
            sessionId = sessionId?:""
        )
        return chatStream(request)
    }


    @GetMapping("/health")
    fun health(): Result<Any> {
        return Result.success("服务正常运行中")
    }

}
