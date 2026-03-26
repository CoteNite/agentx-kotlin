package cn.cotenite.domain.conversation.handler

import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.conversation.constant.Role
import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.llm.model.LLMRequest
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderEntity

/**
 * 对话环境
 */
data class ChatEnvironment(
    var sessionId: String,
    var userId: String,
    var userMessage: String,
    var agent: AgentEntity,
    var model: ModelEntity,
    var provider: ProviderEntity,
    var llmModelConfig: LLMModelConfig,
    var contextEntity: ContextEntity,
    var messageHistory: List<MessageEntity>
) {
    companion object {
        private const val SUMMARY_PREFIX = "以下是用户历史消息的摘要，请仅作为参考，用户没有提起则不要回答摘要中的内容：\n"
    }

    /**
     * 将当前环境转换为 LLMRequest
     */
    fun toLLMRequest(): LLMRequest {
        val messages = buildList<LLMRequest.LLMMessage> {
            agent.systemPrompt
                ?.takeIf { it.isNotBlank() }
                ?.let { add(LLMRequest.LLMMessage(LLMRequest.MessageType.SYSTEM, it)) }

            contextEntity.summary
                ?.takeIf { it.isNotBlank() }
                ?.let { add(LLMRequest.LLMMessage(LLMRequest.MessageType.ASSISTANT, SUMMARY_PREFIX + it)) }

            messageHistory.forEach { message ->
                val content = message.content.orEmpty()
                when (message.role) {
                    Role.USER -> add(LLMRequest.LLMMessage(LLMRequest.MessageType.USER, content))
                    Role.SYSTEM, Role.ASSISTANT -> add(LLMRequest.LLMMessage(LLMRequest.MessageType.ASSISTANT, content))
                    null -> Unit
                }
            }

            add(LLMRequest.LLMMessage(LLMRequest.MessageType.USER, userMessage))
        }

        return LLMRequest(
            messages = messages,
            parameters = LLMRequest.LLMRequestParameters(
                modelId = model.modelId ?: "",
                temperature = llmModelConfig.temperature ?: 0.7,
                topP = llmModelConfig.topP ?: 1.0
            )
        )
    }
}
