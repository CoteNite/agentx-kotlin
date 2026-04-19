package cn.cotenite.infrastructure.llm.factory

import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import java.time.Duration

object LLMProviderFactory {

    fun getLLMProvider(protocol: ProviderProtocol, providerConfig: ProviderConfig): ChatModel {
        var model: ChatModel? = null
        if (protocol == ProviderProtocol.OPENAI) {
            val openAiChatModelBuilder = OpenAiChatModel.OpenAiChatModelBuilder()
            openAiChatModelBuilder.apiKey(providerConfig.apiKey)
            openAiChatModelBuilder.baseUrl(providerConfig.baseUrl)
            openAiChatModelBuilder.customHeaders(providerConfig.customHeaders)
            openAiChatModelBuilder.modelName(providerConfig.model)
            openAiChatModelBuilder.timeout(Duration.ofHours(1))
            model = OpenAiChatModel(openAiChatModelBuilder)
        } else if (protocol == ProviderProtocol.ANTHROPIC) {
            model = AnthropicChatModel.builder()
                .apiKey(providerConfig.apiKey)
                .baseUrl(providerConfig.baseUrl)
                .modelName(providerConfig.model)
                .version("2023-06-01")
                .timeout(Duration.ofHours(1))
                .build()
        }
        return model ?: throw IllegalArgumentException("Unknown provider protocol: $protocol")
    }

    fun getLLMProviderByStream(protocol: ProviderProtocol, providerConfig: ProviderConfig): StreamingChatModel {
        var model: StreamingChatModel? = null
        if (protocol == ProviderProtocol.OPENAI) {
            model = OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder()
                .apiKey(providerConfig.apiKey)
                .baseUrl(providerConfig.baseUrl)
                .customHeaders(providerConfig.customHeaders)
                .modelName(providerConfig.model)
                .timeout(Duration.ofHours(1))
                .build()
        } else if (protocol == ProviderProtocol.ANTHROPIC) {
            model = AnthropicStreamingChatModel.builder()
                .apiKey(providerConfig.apiKey)
                .baseUrl(providerConfig.baseUrl)
                .version("2023-06-01")
                .modelName(providerConfig.model)
                .timeout(Duration.ofHours(1))
                .build()
        }

        return model ?: throw IllegalArgumentException("Unknown provider protocol: $protocol")
    }
}
