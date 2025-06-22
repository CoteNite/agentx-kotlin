package cn.cotenite.agentxkotlin.infrastructure.llm.service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:04
 */
class OpenAILLMRequest(
    val request: Any
): LLMRequest {

    override fun getUnderlyingRequest(): Any {
        return request
    }

}