package cn.cotenite.agentxkotlin.application.conversation.dto

import jakarta.validation.constraints.NotBlank

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:43
 */
class StreamChatRequest(
    @NotBlank(message = "消息内容不能为空")
    override val message: String?,
    override var provider: String?,
    override val model: String?,
    override val sessionId: String?,
    val stream: Boolean=true
):  ChatRequest(message=message,provider=provider,model=model,sessionId=sessionId){
}
