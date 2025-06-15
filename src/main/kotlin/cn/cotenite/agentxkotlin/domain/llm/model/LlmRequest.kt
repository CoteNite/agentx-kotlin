package cn.cotenite.agentxkotlin.domain.llm.model

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:33
 */
data class LlmRequest(
    var model:String = "default",
    val messages: MutableList<LlmMessage> = mutableListOf(),
    val temperature: Double=0.7,
    val maxTokens: Int=512,
    var stream: Boolean=false
){
    fun addMessage(message: LlmMessage): LlmRequest {
        messages.add(message)
        return this
    }

    fun addUserMessage(content: String): LlmRequest {
        return addMessage(LlmMessage.ofUser(content));
    }

    fun addSystemMessage(content: String): LlmRequest {
        return addMessage(LlmMessage.ofSystem(content));
    }

    fun addAssistantMessage(content: String): LlmRequest {
        return addMessage(LlmMessage.ofAssistant(content));
    }

}
