package cn.cotenite.agentxkotlin.infrastructure.llm

import cn.cotenite.agentxkotlin.domain.llm.model.ModelEntity
import cn.cotenite.agentxkotlin.domain.llm.model.ProviderEntity
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.infrastructure.llm.LLMProviderService.getStream
import cn.cotenite.agentxkotlin.infrastructure.llm.config.ProviderConfig
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import org.springframework.stereotype.Component


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 01:09
 */
@Component
class LLMServiceFactory {

    /**
     * 获取流式LLM客户端
     *
     * @param provider 服务商实体
     * @param model 模型实体
     * @return 流式聊天语言模型
     */
    fun getStreamingClient(provider: ProviderEntity, model: ModelEntity): StreamingChatLanguageModel? {
        val config= provider.config?:throw BusinessException("Provider config is null")

        val providerConfig= ProviderConfig(
            apiKey = config.apiKey,
            baseUrl = config.baseUrl,
            model = model.modelId,
            protocol = provider.protocol
        )

        return getStream(provider.protocol, providerConfig)
    }
}