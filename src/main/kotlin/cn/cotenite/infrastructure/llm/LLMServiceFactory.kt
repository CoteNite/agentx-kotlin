package cn.cotenite.infrastructure.llm

import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import org.springframework.stereotype.Component

/**
 * LLM 服务工厂 - 支持 coroutines 和 Flow
 */
@Component
class LLMServiceFactory{

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
     * 获取标准LLM客户端
     *
     * @param provider 服务商实体
     * @param model 模型实体
     * @return 流式聊天语言模型
     */
    fun getStrandClient(provider: ProviderEntity, model: ModelEntity): ChatModel {
        val config = provider.config
        val protocol=provider.protocol?:throw BusinessException("服务商协议不存在")

        val providerConfig = ProviderConfig(
            config?.apiKey,
            config?.baseUrl,
            model.modelId,
            protocol
        )

        return LLMProviderService.getNormal(protocol, providerConfig)
    }


}
