package cn.cotenite.domain.llm.model

/**
 * LLM请求模型
 */
data class LLMRequest(
    val messages: List<LLMMessage>,
    val parameters: LLMRequestParameters
) {
    enum class MessageType {
        USER,
        SYSTEM,
        ASSISTANT
    }

    data class LLMMessage(
        val type: MessageType,
        val content: String
    )

    data class LLMRequestParameters(
        val modelId: String,
        val temperature: Double,
        val topP: Double
    )

    class Builder {
        private var messages: List<LLMMessage> = emptyList()
        private var parameters: LLMRequestParameters = LLMRequestParameters("", 0.7, 1.0)

        fun messages(messages: List<LLMMessage>) = apply { this.messages = messages }

        fun parameters(parameters: LLMRequestParameters) = apply { this.parameters = parameters }

        fun build(): LLMRequest = LLMRequest(messages, parameters)
    }
}
