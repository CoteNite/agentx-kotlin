package cn.cotenite.infrastructure.llm.service

import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply
import dev.langchain4j.model.chat.StreamingChatModel
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component
import cn.cotenite.domain.llm.model.LLMRequest
import cn.cotenite.infrastructure.llm.adapter.LangChain4jAdapter

/**
 * 流式响应处理器 - 使用 coroutines 和 Flow
 */
@Component
class StreamResponseHandler(
    private val adapter: LangChain4jAdapter
) {

    fun handleStreamResponse(
        chatStreamClient: StreamingChatModel,
        llmRequest: LLMRequest
    ): Flow<StreamingChatModelReply> =
        adapter.streamingChat(chatStreamClient, llmRequest)
}
