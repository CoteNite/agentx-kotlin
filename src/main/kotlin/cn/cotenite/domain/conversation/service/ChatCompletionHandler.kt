package cn.cotenite.domain.conversation.service

import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.model.MessageEntity

/**
 * 聊天完成处理器
 */
interface ChatCompletionHandler {

    fun handleCompletion(
        userMessage: MessageEntity,
        llmMessage: MessageEntity,
        contextEntity: ContextEntity,
        inputTokenCount: Int?,
        outputTokenCount: Int?,
        llmContent: String
    )
}
