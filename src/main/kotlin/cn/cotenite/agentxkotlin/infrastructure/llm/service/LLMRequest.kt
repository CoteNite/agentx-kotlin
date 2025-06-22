package cn.cotenite.agentxkotlin.infrastructure.llm.service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 01:11
 */
interface LLMRequest {
    fun getUnderlyingRequest(): Any
}