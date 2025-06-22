package cn.cotenite.agentxkotlin.infrastructure.llm.service

import cn.cotenite.agentxkotlin.domain.llm.model.LLMRequest
import cn.cotenite.agentxkotlin.domain.llm.service.CompletionCallback
import cn.cotenite.agentxkotlin.infrastructure.llm.adapter.LangChain4jAdapter
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import org.springframework.stereotype.Component


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:16
 */
@Component
class StreamResponseHandler(
    private val adapter: LangChain4jAdapter
){

    /**
     * 处理LLM的流式响应
     *
     * @param chatStreamClient 聊天流客户端
     * @param llmRequest       LLM请求
     * @param callback         完成回调接口
     */
    fun handleStreamResponse(
        chatStreamClient: StreamingChatLanguageModel,
        llmRequest: LLMRequest,
        callback: CompletionCallback
    ) {
        adapter.doStreamingChat(chatStreamClient, llmRequest, callback)
    }



}