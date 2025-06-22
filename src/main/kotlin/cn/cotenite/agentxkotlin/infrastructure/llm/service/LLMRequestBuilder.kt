package cn.cotenite.agentxkotlin.infrastructure.llm.service

import cn.cotenite.agentxkotlin.domain.conversation.constant.Role
import cn.cotenite.agentxkotlin.domain.conversation.constant.Role.*
import cn.cotenite.agentxkotlin.domain.conversation.model.ContextEntity
import cn.cotenite.agentxkotlin.domain.conversation.service.ContextProcessor
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.Content
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.openai.OpenAiChatRequestParameters
import org.springframework.stereotype.Component


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 01:12
 */
@Component
class LLMRequestBuilder {

    /**
     * 构建LLM请求
     *
     * @param contextResult   上下文处理结果
     * @param userMessage     用户消息
     * @param systemPrompt    系统提示语
     * @param modelId         模型ID
     * @param temperature     温度参数
     * @param topP            topP参数
     * @return LLM请求对象
     */
    fun buildRequest(
        contextResult: ContextProcessor.ContextResult,
        userMessage: String,
        systemPrompt: String,
        modelId: String?,
        temperature: Float,
        topP: Float
    ): LLMRequest {
        val chatMessages = mutableListOf<ChatMessage>()
        val chatRequestBuilder = ChatRequest.builder()

        val userContents = mutableListOf<Content>()
        val systemContents = mutableListOf<Content>()

        contextResult.messageEntities.forEach {messageEntity ->
            val role = messageEntity.role
            val content = messageEntity.content
            if (role == USER) {
                userContents.add(TextContent(content));
            } else if (role == SYSTEM) {
                systemContents.add(TextContent(content));
            }
        }

        val contextEntity = contextResult.contextEntity

        if (contextEntity.summary?.isNotEmpty() == true){
            val preStr = "以下是用户之前的历史消息提炼成的摘要消息"
            chatMessages.add(AiMessage(preStr+contextEntity.summary))
        }

        userContents.add(TextContent(userMessage))
        chatMessages.add(UserMessage(userContents))

        if (systemPrompt.isNotEmpty()) {
            chatMessages.add(SystemMessage(systemPrompt))
        }

        val parameters = OpenAiChatRequestParameters.builder()
        parameters.modelName(modelId)

        val tempDouble = temperature.toDouble()
        val topPDouble = topP.toDouble()

        parameters.topP(topPDouble).temperature(tempDouble)

        chatRequestBuilder.messages(chatMessages)
        chatRequestBuilder.parameters(parameters.build())

        return OpenAILLMRequest(chatRequestBuilder.build())
    }

}