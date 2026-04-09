package cn.cotenite.infrastructure.llm

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.llm.factory.LLMProviderFactory
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * LLM Provider服务
 */
object LLMProviderService {

    fun getStrand(protocol: ProviderProtocol, providerConfig: ProviderConfig): ChatModel =
        LLMProviderFactory.getLLMProvider(protocol, providerConfig)

    fun getStream(protocol: ProviderProtocol, providerConfig: ProviderConfig): StreamingChatModel =
        LLMProviderFactory.getLLMProviderByStream(protocol, providerConfig)
}
