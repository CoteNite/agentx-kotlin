package cn.cotenite.infrastructure.llm.factory

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * LLM Provider工厂
 */
object LLMProviderFactory {

    fun getLLMProvider(protocol: ProviderProtocol, providerConfig: ProviderConfig): ChatModel =
        when (protocol) {
            ProviderProtocol.OPENAI -> OpenAiChatModel.builder()
                .apiKey(providerConfig.apiKey)
                .baseUrl(providerConfig.baseUrl)
                .modelName(providerConfig.model)
                .apply {
                    if (providerConfig.customHeaders.isNotEmpty()) {
                        customHeaders(providerConfig.customHeaders)
                    }
                }
                .build()

            else -> throw BusinessException("不支持的服务商协议: ${protocol.code}")
        }

    fun getLLMProviderByStream(
        protocol: ProviderProtocol,
        providerConfig: ProviderConfig
    ): StreamingChatModel =
        when (protocol) {
            ProviderProtocol.OPENAI -> OpenAiStreamingChatModel.builder()
                .apiKey(providerConfig.apiKey)
                .baseUrl(providerConfig.baseUrl)
                .modelName(providerConfig.model)
                .apply {
                    if (providerConfig.customHeaders.isNotEmpty()) {
                        customHeaders(providerConfig.customHeaders)
                    }
                }
                .build()

            else -> throw BusinessException("不支持的服务商协议: ${protocol.code}")
        }
}
