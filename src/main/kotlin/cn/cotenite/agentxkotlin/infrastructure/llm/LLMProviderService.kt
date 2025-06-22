package cn.cotenite.agentxkotlin.infrastructure.llm

import cn.cotenite.agentxkotlin.infrastructure.llm.config.ProviderConfig
import cn.cotenite.agentxkotlin.infrastructure.llm.factory.LLMProviderFactory
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:17
 */
@Service
class LLMProviderService {

    fun getNormal(protocol: ProviderProtocol, providerConfig: ProviderConfig): ChatLanguageModel? {
        return LLMProviderFactory.getLLMProvider(protocol, providerConfig)
    }


    fun getStream(protocol: ProviderProtocol, providerConfig: ProviderConfig): StreamingChatLanguageModel? {
        return LLMProviderFactory.getLLMProviderByStream(protocol, providerConfig)
    }

}