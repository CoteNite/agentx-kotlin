package cn.cotenite.agentxkotlin.domain.llm.model

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:37
 */
data class LlmResponse(
    var content:String="",
    val provider:String,
    val model:String,
    var finishReason:String="",
    var tokenUsage:Int=0
) {
}
