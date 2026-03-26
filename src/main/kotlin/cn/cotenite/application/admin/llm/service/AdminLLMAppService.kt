package cn.cotenite.application.admin.llm.service

import org.springframework.stereotype.Service
import cn.cotenite.application.llm.assembler.ModelAssembler
import cn.cotenite.application.llm.assembler.ProviderAssembler
import cn.cotenite.application.llm.dto.ModelDTO
import cn.cotenite.application.llm.dto.ProviderDTO
import cn.cotenite.domain.llm.service.LlmDomainService
import cn.cotenite.infrastructure.entity.Operator
import cn.cotenite.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.interfaces.dto.llm.ModelUpdateRequest
import cn.cotenite.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.interfaces.dto.llm.ProviderUpdateRequest

/**
 * 管理端LLM应用服务
 */
@Service
class AdminLLMAppService(
    private val llmDomainService: LlmDomainService
) {

    fun createProvider(providerCreateRequest: ProviderCreateRequest, userId: String): ProviderDTO? =
        ProviderAssembler.toEntity(providerCreateRequest, userId)
            .apply {
                setAdmin()
                isOfficial = true
                status = providerCreateRequest.status ?: true
            }
            .let(llmDomainService::createProvider)
            .let(ProviderAssembler::toDTO)

    fun updateProvider(providerUpdateRequest: ProviderUpdateRequest, userId: String): ProviderDTO? {
        val provider = ProviderAssembler.toEntity(providerUpdateRequest, userId).apply { setAdmin() }
        llmDomainService.updateProvider(provider)
        return provider.id?.let(llmDomainService::getProvider)?.let(ProviderAssembler::toDTO)
    }

    fun deleteProvider(providerId: String, userId: String) =
        llmDomainService.deleteProvider(providerId, userId, Operator.ADMIN)

    fun createModel(modelCreateRequest: ModelCreateRequest, userId: String): ModelDTO? =
        ModelAssembler.toEntity(modelCreateRequest, userId)
            .apply {
                setAdmin()
                isOfficial = true
                status = true
            }
            .also(llmDomainService::createModel)
            .let(ModelAssembler::toDTO)

    fun updateModel(modelUpdateRequest: ModelUpdateRequest, userId: String): ModelDTO? {
        val model = ModelAssembler.toEntity(modelUpdateRequest, userId).apply { setAdmin() }
        llmDomainService.updateModel(model)
        return model.id?.let(llmDomainService::getModelById)?.let(ModelAssembler::toDTO)
    }

    fun deleteModel(modelId: String, userId: String) =
        llmDomainService.deleteModel(modelId, userId, Operator.ADMIN)
}
