package cn.cotenite.agentxkotlin.domain.llm.model

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 23:26
 */
data class LLMRequest(
    val messages: List<LLMMessage>,
    val parameters: LLMRequestParameters
) {

    /**
     * 消息类型
     */
    enum class MessageType {
        USER,
        SYSTEM,
        ASSISTANT
    }

    /**
     * LLM消息模型
     */
    data class LLMMessage(
        val type: MessageType,
        val content: String
    )
    // LLMMessage 的构造函数和属性 (type, content) 在数据类定义时自动生成

    /**
     * LLM请求参数
     */
    data class LLMRequestParameters(
        val modelId: String,
        val temperature: Double? = null, // 允许为 null，或者可以给个默认值 0.7
        val topP: Double? = null // 允许为 null，或者可以给个默认值 0.7
    )
}