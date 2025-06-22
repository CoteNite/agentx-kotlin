package cn.cotenite.agentxkotlin.domain.llm.service

import cn.cotenite.agentxkotlin.domain.conversation.service.ContextProcessor
import cn.cotenite.agentxkotlin.domain.llm.model.LLMRequest

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 23:25
 */
interface LLMRequestService{

    /**
     * 构建LLM请求
     *
     * @param contextResult   上下文处理结果
     * @param userMessage     用户消息
     * @param systemPrompt    系统提示语
     * @param modelId         模型ID
     * @param temperature     温度参数
     * @param topP            topP参数
     * @return 构建好的领域请求对象
     */
    fun buildRequest(
        contextResult: ContextProcessor.ContextResult,
        userMessage: String,
        systemPrompt: String,
        modelId: String,
        temperature: Float,
        topP: Float
    ): LLMRequest

}