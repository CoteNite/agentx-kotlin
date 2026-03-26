package cn.cotenite.infrastructure.llm.adapter

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply
import dev.langchain4j.kotlin.model.chat.chatFlow
import dev.langchain4j.model.chat.StreamingChatModel
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component
import cn.cotenite.domain.llm.model.LLMRequest

/**
 * LangChain4j Kotlin 适配器
 * 使用 coroutines 和 Flow 替代 callback-based 的流式处理
 */
@Component
class LangChain4jAdapter {

    /**
     * 流式聊天 - 使用 LangChain4j Kotlin 的 chatFlow 扩展返回 Flow
     */
    fun streamingChat(
        client: StreamingChatModel,
        llmRequest: LLMRequest
    ): Flow<StreamingChatModelReply> = client.chatFlow {
        messages += llmRequest.messages.map { message ->
            when (message.type) {
                LLMRequest.MessageType.USER -> UserMessage(message.content)
                LLMRequest.MessageType.SYSTEM -> SystemMessage(message.content)
                LLMRequest.MessageType.ASSISTANT -> AiMessage(message.content)
            }
        }
        parameters {
            modelName = llmRequest.parameters.modelId
            temperature = llmRequest.parameters.temperature
            topP = llmRequest.parameters.topP
        }
    }
}
