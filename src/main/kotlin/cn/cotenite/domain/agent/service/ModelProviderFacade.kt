package cn.cotenite.domain.agent.service

import dev.langchain4j.model.chat.StreamingChatModel
import org.springframework.stereotype.Service
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.domain.llm.service.LlmDomainService
import cn.cotenite.infrastructure.llm.LLMProviderService
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * 模型提供商门面
 */
@Service
class ModelProviderFacade(
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val llmDomainService: LlmDomainService
) {

    fun getModelAndProvider(agentId: String, userId: String): ModelProviderResult {
        val llmModelConfig = agentWorkspaceDomainService.getWorkspace(agentId, userId).llmModelConfig
        val model = llmDomainService.getModelById(llmModelConfig.modelId.orEmpty()).also { it.isActive() }
        val provider = llmDomainService.getProvider(model.providerId.orEmpty(), userId).also { it.isActive() }

        val providerConfig = ProviderConfig(
            apiKey = provider.config?.apiKey,
            baseUrl = provider.config?.baseUrl,
            model = model.modelId,
            protocol = provider.protocol ?: ProviderProtocol.OTHER
        )

        return ModelProviderResult(
            modelEntity = model,
            providerEntity = provider,
            llmModelConfig = llmModelConfig,
            providerConfig = providerConfig,
            chatStreamClient = LLMProviderService.getStream(providerConfig.protocol, providerConfig)
        )
    }

    data class ModelProviderResult(
        val modelEntity: ModelEntity,
        val providerEntity: ProviderEntity,
        val llmModelConfig: LLMModelConfig,
        val providerConfig: ProviderConfig,
        val chatStreamClient: StreamingChatModel
    )
}
