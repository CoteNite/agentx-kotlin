package cn.cotenite.application.conversation.service.handler.context

import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.conversation.constant.Role.SYSTEM
import cn.cotenite.domain.conversation.constant.Role.USER
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
 * 对话上下文
 */
data class ChatContext(
    val sessionId: String,
    val userId: String,
    val userMessage: String,
    val agent: AgentEntity,
    val model: ModelEntity,
    val provider: ProviderEntity,
    val llmModelConfig: LLMModelConfig,
    var contextEntity: ContextEntity?=null,
    var messageHistory: List<MessageEntity>?=null,
    val mcpServerName: List<String?>,
    /** 高可用实例ID  */
    var instanceId: String? = null,
    val fileUrls: MutableList<String?>?=null
) {

    fun prepareChatRequest(): dev.langchain4j.model.chat.request.ChatRequest {
        val chatMessages = mutableListOf<ChatMessage>()

        agent.systemPrompt
            ?.takeIf { it.isNotBlank() }
            ?.let { chatMessages.add(SystemMessage(it)) }

        contextEntity?.summary
            ?.takeIf { it.isNotBlank() }
            ?.let { chatMessages.add(AiMessage(AgentPromptTemplates.SUMMARY_PREFIX + it)) }

        messageHistory?.forEach { messageEntity ->
            when (messageEntity.role) {
                USER -> chatMessages.add(UserMessage(messageEntity.content ?: ""))
                SYSTEM -> chatMessages.add(AiMessage(messageEntity.content ?: ""))
                else -> {}
            }
        }

        chatMessages.add(UserMessage(userMessage))

        val parameters = OpenAiChatRequestParameters.builder()
            .modelName(model.modelId)
            .topP(llmModelConfig.topP)
            .temperature(llmModelConfig.temperature)
            .build()

        return dev.langchain4j.model.chat.request.ChatRequest.builder()
            .messages(chatMessages)
            .parameters(parameters)
            .build()
    }
}
