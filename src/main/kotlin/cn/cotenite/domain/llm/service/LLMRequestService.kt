package cn.cotenite.domain.llm.service

import cn.cotenite.domain.conversation.service.ContextProcessor
import cn.cotenite.domain.llm.model.LLMRequest

/**
 * LLM 请求服务接口
 */
interface LLMRequestService {

    fun buildRequest(
        contextResult: ContextProcessor.ContextResult,
        userMessage: String,
        systemPrompt: String,
        modelId: String,
        temperature: Float,
        topP: Float
    ): LLMRequest
}
