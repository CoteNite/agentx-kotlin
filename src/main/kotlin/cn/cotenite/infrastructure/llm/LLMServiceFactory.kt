package cn.cotenite.infrastructure.llm

import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply
import dev.langchain4j.model.chat.StreamingChatModel
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Component
import cn.cotenite.domain.conversation.handler.ChatEnvironment
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.llm.service.StreamResponseHandler

/**
 * LLM 服务工厂 - 支持 coroutines 和 Flow
 */
@Component
class LLMServiceFactory(
    private val streamResponseHandler: StreamResponseHandler
) {

    fun getStreamingClient(provider: ProviderEntity, model: ModelEntity): StreamingChatModel {
        val protocol = provider.protocol ?: throw BusinessException("服务商协议不存在")
        val config = ProviderConfig(
            apiKey = provider.config?.apiKey ?: throw BusinessException("服务商配置不存在"),
            baseUrl = provider.config?.baseUrl,
            model = model.modelId,
            protocol = protocol
        )
        return LLMProviderService.getStream(protocol, config)
    }

    /**
     * 获取流式响应 Flow
     */
    fun getStreamingFlow(
        environment: ChatEnvironment,
        userMessageEntity: MessageEntity,
        llmMessageEntity: MessageEntity
    ): Flow<StreamingChatModelReply> {
        val llmClient = getStreamingClient(environment.provider, environment.model)
        val llmRequest = environment.toLLMRequest()
        return streamResponseHandler.handleStreamResponse(llmClient, llmRequest)
    }
}
