package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.ContextEntity
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity



/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 23:32
 */
interface ChatCompletionHandler {

    fun handleCompletion(
        userMessage: MessageEntity?,
        llmMessage: MessageEntity?,
        contextEntity: ContextEntity?,
        inputTokenCount: Int?,
        outputTokenCount: Int?,
        llmContent: String?
    )

}