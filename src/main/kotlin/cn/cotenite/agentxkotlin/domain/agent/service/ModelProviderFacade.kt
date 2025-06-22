package cn.cotenite.agentxkotlin.domain.agent.service

import cn.cotenite.agentxkotlin.domain.llm.model.ModelEntity
import cn.cotenite.agentxkotlin.domain.llm.model.ProviderEntity
import cn.cotenite.agentxkotlin.domain.llm.model.config.LLMModelConfig
import cn.cotenite.agentxkotlin.domain.llm.model.config.ProviderConfig
import cn.cotenite.agentxkotlin.domain.llm.service.LlmDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.infrastructure.llm.LLMProviderService
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import org.springframework.stereotype.Service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 22:58
 */
@Service
class ModelProviderFacade(
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val llmDomainService: LlmDomainService
){

    fun getModelAndProvide(agentId: String, userId: String): ModelProviderResult {

        val workspace = agentWorkspaceDomainService.getWorkspace(agentId, userId)
        val llmModelConfig = workspace.llmModelConfig
        val modelId = llmModelConfig.modelId?:throw Exception("请选择模型")
        val model = llmDomainService.getModelById(modelId)

        model.isActive()

        val provider =llmDomainService.getProvider(model.providerId, userId)
        provider.isActive()

        val domainConfig = provider.config?:throw BusinessException("模型提供者配置不存在")
        val providerConfig = cn.cotenite.agentxkotlin.infrastructure.llm.config.ProviderConfig(
            apiKey = domainConfig.apiKey,
            baseUrl = domainConfig.baseUrl,
            model = model.modelId,
            protocol = provider.protocol,
        )

        val chatStreamClient = LLMProviderService.getStream(provider.protocol, providerConfig)?:throw BusinessException("模型提供者配置不存在")
        return ModelProviderResult(model, provider, llmModelConfig, providerConfig, chatStreamClient)
    }

    companion object{
        data class ModelProviderResult(
            val modelEntity: ModelEntity,
            val providerEntity: ProviderEntity,
            val llmModelConfig: LLMModelConfig,
            val providerConfig: cn.cotenite.agentxkotlin.infrastructure.llm.config.ProviderConfig,
            val chatStreamClient: StreamingChatLanguageModel
        )
    }

}