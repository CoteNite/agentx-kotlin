package cn.cotenite.agentxkotlin.infrastructure.llm.adapter

import cn.cotenite.agentxkotlin.domain.llm.model.LLMRequest
import cn.cotenite.agentxkotlin.domain.llm.service.CompletionCallback
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import dev.langchain4j.model.openai.OpenAiChatRequestParameters
import org.springframework.stereotype.Component

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 00:56
 */
@Component
class LangChain4jAdapter {

    fun toExternalRequest(llmRequest: LLMRequest): ChatRequest {
        val chatMessages = mutableListOf<ChatMessage>()

        llmRequest.messages.forEach { message ->
            when (message.type) {
                LLMRequest.MessageType.USER -> chatMessages.add(UserMessage(message.content))
                LLMRequest.MessageType.SYSTEM -> chatMessages.add(SystemMessage(message.content))
                LLMRequest.MessageType.ASSISTANT -> chatMessages.add(AiMessage(message.content))
            }
        }

        val params = llmRequest.parameters
        val parameters = OpenAiChatRequestParameters.builder()
            .modelName(params.modelId)
            .temperature(params.temperature)
            .topP(params.topP)
            .build()

        return ChatRequest.builder()
            .messages(chatMessages)
            .parameters(parameters)
            .build()
    }

    fun doStreamingChat(
        client: StreamingChatLanguageModel,
        llmRequest: LLMRequest,
        callback: CompletionCallback
    ){

        val externalRequest = this.toExternalRequest(llmRequest)

        client.doChat(externalRequest,object : StreamingChatResponseHandler{

            override fun onPartialResponse(partialResponse: String) {
                callback.onPartialResponse(partialResponse)
            }

            override fun onCompleteResponse(response: ChatResponse) {
                val content = response.aiMessage().text()
                val inputTokens = response.metadata().tokenUsage().inputTokenCount()
                val outputTokens = response.metadata().tokenUsage().outputTokenCount()

                callback.onCompleteResponse(content, inputTokens, outputTokens)
            }

            override fun onError(error: Throwable){
                callback.onError(error)
            }

        })
    }

}