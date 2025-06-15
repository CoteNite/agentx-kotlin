package cn.cotenite.agentxkotlin.domain.llm.model

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:32
 */
data class LlmMessage(
    val role: String,
    val content: String
){

    companion object{
        fun ofUser(content: String): LlmMessage {
            return LlmMessage("user", content)
        }

        fun ofAssistant(content: String): LlmMessage {
            return LlmMessage("assistant", content)
        }

        fun ofSystem(content: String): LlmMessage {
            return LlmMessage("system", content)
        }
    }


}
