package cn.cotenite.application.conversation.service.handler.context

import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.conversation.constant.Role.*
import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderEntity
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.openai.OpenAiChatRequestParameters

/**
 * @author  yhk
 * Description  
 * Date  2026/3/28 21:30
 */
data class ChatContext(
    val sessionId: String,
    val userId: String,
    val userMessage: String,
    val agent: AgentEntity,
    val model: ModelEntity,
    val provider: ProviderEntity,
    val llmModelConfig: LLMModelConfig,
    val contextEntity: ContextEntity,
    val messageHistory: List<MessageEntity>
){

    fun prepareChatRequest(): dev.langchain4j.model.chat.request.ChatRequest {
        val chatMessages=mutableListOf<ChatMessage>()

        this.agent.systemPrompt?.isNotEmpty().let {
            chatMessages.add(SystemMessage(this.agent.systemPrompt))
        }

        this.contextEntity.summary?.isNotEmpty().let {
            chatMessages.add(AiMessage(AgentPromptTemplate.SUMMARY_PREFIX+this.contextEntity.summary))
        }

        this.messageHistory.forEach { messageEntity ->
            when(messageEntity.role){
                USER -> chatMessages.add(UserMessage(messageEntity.content))
                SYSTEM -> chatMessages.add(AiMessage(messageEntity.content))
                else -> {}
            }
        }

        chatMessages.add(UserMessage(this.userMessage))

        val parameters = OpenAiChatRequestParameters.builder()
            .modelName(model.modelId)
            .topP(llmModelConfig.topP)
            .temperature(llmModelConfig.temperature)
            .build()


        return dev.langchain4j.model.chat.request.ChatRequest.Builder()
            .messages(chatMessages)
            .parameters(parameters)
            .build()
    }



}