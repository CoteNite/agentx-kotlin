package cn.cotenite.application.llm.service

import org.springframework.stereotype.Service
import cn.cotenite.application.llm.assembler.ModelAssembler
import cn.cotenite.application.llm.assembler.ProviderAssembler
import cn.cotenite.application.llm.dto.ModelDTO
import cn.cotenite.application.llm.dto.ProviderDTO
import cn.cotenite.domain.llm.model.enums.ModelType
import cn.cotenite.domain.llm.model.enums.ProviderType
import cn.cotenite.domain.llm.service.LlmDomainService
import cn.cotenite.infrastructure.entity.Operator
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import cn.cotenite.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.interfaces.dto.llm.ModelUpdateRequest
import cn.cotenite.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.interfaces.dto.llm.ProviderUpdateRequest

/**
 * LLM应用服务
 */
@Service
class LLMAppService(
    private val llmDomainService: LlmDomainService
) {

    fun getProviderDetail(providerId: String, userId: String): ProviderDTO? =
        llmDomainService.getProviderAggregate(providerId, userId).let(ProviderAssembler::toDTO)

    fun createProvider(providerCreateRequest: ProviderCreateRequest, userId: String): ProviderDTO? =
        ProviderAssembler.toEntity(providerCreateRequest, userId)
            .apply { isOfficial = false }
            .let(llmDomainService::createProvider)
            .let(ProviderAssembler::toDTO)

    fun updateProvider(providerUpdateRequest: ProviderUpdateRequest, userId: String): ProviderDTO? {
        val providerId = providerUpdateRequest.id ?: return null
        val existingProvider = llmDomainService.getProvider(providerId, userId)

        if (providerUpdateRequest.config?.apiKey?.matches(Regex("\\*+")) == true) {
            providerUpdateRequest.config?.apiKey = existingProvider.config?.apiKey
        }

        val provider = ProviderAssembler.toEntity(providerUpdateRequest, userId)
        llmDomainService.updateProvider(provider)
        return ProviderAssembler.toDTO(provider)
    }

    fun getProvider(providerId: String, userId: String): ProviderDTO? = getProviderDetail(providerId, userId)

    fun deleteProvider(providerId: String, userId: String) =
        llmDomainService.deleteProvider(providerId, userId, Operator.USER)

    fun getUserProviders(userId: String): List<ProviderDTO> =
        llmDomainService.getUserProviders(userId).mapNotNull(ProviderAssembler::toDTO)

    fun getAllProviders(userId: String): List<ProviderDTO> =
        llmDomainService.getAllProviders(userId).mapNotNull(ProviderAssembler::toDTO)

    fun getOfficialProviders(): List<ProviderDTO> =
        llmDomainService.getOfficialProviders().mapNotNull(ProviderAssembler::toDTO)

    fun getCustomProviders(userId: String): List<ProviderDTO> =
        llmDomainService.getCustomProviders(userId).mapNotNull(ProviderAssembler::toDTO)

    fun getUserProviderProtocols(): List<ProviderProtocol> = llmDomainService.getProviderProtocols()

    fun createModel(modelCreateRequest: ModelCreateRequest, userId: String): ModelDTO? {
        llmDomainService.checkProviderExists(modelCreateRequest.providerId ?: return null, userId)
        val model = ModelAssembler.toEntity(modelCreateRequest, userId).apply {
            isOfficial = false
        }
        llmDomainService.createModel(model)
        return ModelAssembler.toDTO(model)
    }

    fun updateModel(modelUpdateRequest: ModelUpdateRequest, userId: String): ModelDTO? {
        val model = ModelAssembler.toEntity(modelUpdateRequest, userId)
        llmDomainService.updateModel(model)
        return ModelAssembler.toDTO(model)
    }

    fun deleteModel(modelId: String, userId: String) =
        llmDomainService.deleteModel(modelId, userId, Operator.ADMIN)

    fun updateModelStatus(modelId: String, userId: String) =
        llmDomainService.updateModelStatus(modelId, userId)

    fun getProvidersByType(providerType: ProviderType, userId: String): List<ProviderDTO> =
        llmDomainService.getProvidersByType(providerType, userId).mapNotNull(ProviderAssembler::toDTO)

    fun updateProviderStatus(providerId: String, userId: String) =
        llmDomainService.updateProviderStatus(providerId, userId)

    fun getActiveModelsByType(providerType: ProviderType, userId: String, modelType: ModelType?): List<ModelDTO> =
        llmDomainService.getProvidersByType(providerType, userId)
            .filter { it.getStatus() }
            .flatMap { it.getModels() }
            .filter { modelType == null || it.type == modelType }
            .mapNotNull(ModelAssembler::toDTO)
}
