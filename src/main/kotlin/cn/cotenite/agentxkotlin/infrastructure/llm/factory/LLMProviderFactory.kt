package cn.cotenite.agentxkotlin.infrastructure.llm.factory

import cn.cotenite.agentxkotlin.infrastructure.llm.config.ProviderConfig
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:19
 */
object LLMProviderFactory {

    /**
     * 获取对应的服务商
     * 不使用工厂模式，因为 OpenAiChatModel 没有无参构造器，并且其他类型的模型不能适配
     * @param protocol 协议
     * @param providerConfig 服务商信息
     */
    fun getLLMProvider(protocol: ProviderProtocol, providerConfig: ProviderConfig): ChatLanguageModel? {
        return if (protocol == ProviderProtocol.OpenAI) {
            OpenAiChatModel.builder()
                .apiKey(providerConfig.apiKey)
                .baseUrl(providerConfig.baseUrl)
                .customHeaders(providerConfig.customHeaders)
                .build() // 构建模型实例
        } else {
            null // 如果协议不匹配，返回 null
        }
    }

    fun getLLMProviderByStream(protocol: ProviderProtocol, providerConfig: ProviderConfig): StreamingChatLanguageModel? {
        return if (protocol == ProviderProtocol.OpenAI) {
            OpenAiStreamingChatModel.builder()
                .apiKey(providerConfig.apiKey)
                .baseUrl(providerConfig.baseUrl)
                .customHeaders(providerConfig.customHeaders)
                .modelName(providerConfig.model)
                .build() // 构建模型实例
        } else {
            null // 如果协议不匹配，返回 null
        }
    }

}