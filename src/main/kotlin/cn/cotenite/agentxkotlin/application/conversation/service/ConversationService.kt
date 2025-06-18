package cn.cotenite.agentxkotlin.application.conversation.service

import cn.cotenite.agentxkotlin.application.conversation.dto.ChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.ChatResponse
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import cn.cotenite.agentxkotlin.domain.llm.model.LlmRequest
import cn.cotenite.agentxkotlin.domain.llm.service.LlmService
import cn.cotenite.agentxkotlin.infrastructure.integration.llm.siliconflow.SiliconFlowLlmService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:50
 */
@Service
class ConversationService(
    private val defaultLlmService: LlmService,
    private val llmServiceMap:MutableMap<String, LlmService>
){

    private val logger= LoggerFactory.getLogger(ConversationService::class.java)

    fun chat(request: ChatRequest):ChatResponse{
        logger.info("接受到聊天记录:${request.message}")

        val llmService = this.getLlmService(request.provider)

        val llmRequest = LlmRequest()
        llmRequest.addUserMessage(request.message)

        if (request.model.isNotEmpty()){
            logger.info("用户指定使用模型：${request.model}")
            llmRequest.model = request.model
        }else{
            logger.info("使用默认模型：${llmService.getDefaultModel()}")
        }

        val llmResponse = llmService.chat(llmRequest)

        val chatResponse = ChatResponse(
            content = llmResponse.content,
            provider = llmService.getProviderName(),
            model = llmService.getDefaultModel(),
            sessionId = request.sessionId
        )

        return chatResponse
    }

    suspend fun chatStreamList(request: StreamChatRequest): List<StreamChatResponse>{
        logger.info("接收到流式聊天请求: ${request.message}")

        val responses = mutableListOf<StreamChatResponse>()

        val llmService = this.getLlmService(request.provider)

        val llmRequest = LlmRequest(stream = true)
        llmRequest.addUserMessage(request.message)

        if (request.model.isNotEmpty()){
            logger.info("用户指定使用模型：${request.model}")
            llmRequest.model = request.model
        }else{
            logger.info("使用默认模型：${llmService.getDefaultModel()}")
        }

        try {
            val chunks = llmService.chatStreamList(llmRequest)

            chunks.forEachIndexed{ index, chunk ->
                val response = StreamChatResponse(
                    content = chunk,
                    done = index == chunks.size - 1,
                    sessionId = request.sessionId,
                    provider = llmService.getProviderName(),
                    model = llmRequest.model
                )
                responses.add(response)
            }

            if (responses.isEmpty()){
                responses.add(this.createFinalResponse(request, llmService))
            }

        }catch (e:Exception){
            logger.error("处理流式聊天请求异常", e)
            val response = StreamChatResponse(
                content = "处理流式聊天请求异常${e.message}",
                done = true,
                sessionId = request.sessionId,
                provider = llmService.getProviderName(),
                model = llmRequest.model
            )
            responses.add(response)
        }

        return responses
    }

    private fun createFinalResponse(request: StreamChatRequest, llmService: LlmService): StreamChatResponse {
        return StreamChatResponse(
            content = "",
            done = true,
            sessionId = request.sessionId,
            provider = llmService.getProviderName(),
            model = llmService.getDefaultModel()
        )
    }


    private fun getLlmService(provider:String):LlmService{
        if (provider.isEmpty()){
            logger.info("使用默认的LLM服务：${defaultLlmService.getProviderName()}")
            return defaultLlmService
        }

        val serviceName = provider.lowercase() + "LlmService"
        logger.info("尝试获取指定的LLM服务：${serviceName}")

        val service = llmServiceMap[serviceName]

        if (service==null){
            logger.warn("未能找到服务商[${provider}]的实现，使用默认服务商：${defaultLlmService.getProviderName()}")
            return defaultLlmService
        }

        logger.info("使用服务商[${provider}]")

        return service
    }

    suspend fun chatStream(request: StreamChatRequest, responseHandler: (chunk: StreamChatResponse, isLast: Boolean) -> Unit){
        logger.info("接收到真实流式聊天请求: ${request.message}")

        val llmService = this.getLlmService(request.provider)

        val llmRequest = LlmRequest().apply {
            addUserMessage(request.message)
            stream = true

            if (request.model.isEmpty()) {
                logger.info("使用默认模型: ${llmService.getDefaultModel()}")
                model = llmService.getDefaultModel()
            } else {
                logger.info("用户指定模型: ${request.model}")
                model = request.model
            }
        }

        try {
            var isLast = false
            if (llmService is SiliconFlowLlmService){
                logger.info("使用SiliconFlow的真实流式响应")

                llmService.streamChat(llmRequest).collect{chunk->
                    if (chunk == "[DONE]") {
                        isLast = true
                    }
                    val response = StreamChatResponse(
                        content = chunk,
                        done = isLast,
                        sessionId = request.sessionId,
                        provider = llmService.getProviderName(),
                        model = request.model,
                    )
                    responseHandler(response, isLast)
                }
            }else{
                logger.info("服务商不支持真实流式，使用传统分块方式")

                val chunks  = llmService.chatStreamList(llmRequest)

                chunks.forEachIndexed { index, chunk ->
                    val isLast = index == chunks.size - 1
                    val response = StreamChatResponse(
                        content = chunk,
                        sessionId = request.sessionId,
                        provider = llmService.getProviderName(),
                        model = request.model,
                        done = isLast
                    )
                    responseHandler(response,isLast)
                }
            }
        }catch (e:Exception){
            logger.error("处理流式聊天请求异常", e)

            // 发生异常时，返回一个错误响应
            val errorResponse = StreamChatResponse(
                content = "处理流式聊天请求异常:${e.message}",
                done = true,
                sessionId = request.sessionId,
                provider = llmService.getProviderName(),
                model = request.model,
                timestamp = System.currentTimeMillis()
            )
            responseHandler(errorResponse, true)
        }
    }

}
