package cn.cotenite.agentxkotlin.application.llm.service

import cn.cotenite.agentxkotlin.application.llm.assembler.ModelAssembler
import cn.cotenite.agentxkotlin.application.llm.assembler.ProviderAssembler
import cn.cotenite.agentxkotlin.application.llm.dto.ModelDTO
import cn.cotenite.agentxkotlin.application.llm.dto.ProviderDTO
import cn.cotenite.agentxkotlin.domain.llm.model.ModelEntity
import cn.cotenite.agentxkotlin.domain.llm.model.ProviderAggregate
import cn.cotenite.agentxkotlin.domain.llm.model.ProviderEntity
import cn.cotenite.agentxkotlin.domain.llm.model.enums.ModelType
import cn.cotenite.agentxkotlin.domain.llm.model.enums.ProviderType
import cn.cotenite.agentxkotlin.domain.llm.service.LlmDomainService
import cn.cotenite.agentxkotlin.infrastructure.entity.Operator
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelUpdateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderUpdateRequest
import org.springframework.stereotype.Service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:14
 */
@Service
class LLMAppService(
    private val llmDomainService: LlmDomainService,
){

    /**
     * 获取服务商聚合根
     * @param providerId 服务商id
     * @param userId 用户id
     * @return ProviderAggregate
     */
    fun getProviderDetail(providerId: String, userId: String): ProviderDTO {
        val providerAggregate= llmDomainService.getProviderAggregate(providerId, userId)
        return ProviderAssembler.toDTO(providerAggregate)
    }

    /**
     * 创建服务商
     * @param providerCreateRequest 请求对象
     * @param userId 用户id
     * @return ProviderDTO
     */
    fun createProvider(providerCreateRequest: ProviderCreateRequest, userId: String): ProviderDTO {
        val provider = ProviderAssembler.toEntity(providerCreateRequest, userId)
        provider.isOfficial=false
        llmDomainService.createProvider(provider)
        return ProviderAssembler.toDTO(provider)
    }

    /**
     * 更新服务商
     * @param providerUpdateRequest 更新对象
     * @param userId 用户id
     * @return ProviderDTO
     */
    fun updateProvider(providerUpdateRequest: ProviderUpdateRequest, userId: String): ProviderDTO {
        val provider: ProviderEntity = ProviderAssembler.toEntity(providerUpdateRequest, userId)
        llmDomainService.updateProvider(provider)
        return ProviderAssembler.toDTO(provider)
    }


    /**
     * 获取服务商
     * @param providerId 服务商id
     * @return ProviderDTO
     */
    fun getProvider(providerId: String, userId: String): ProviderDTO {
        val provider: ProviderEntity = llmDomainService.getProvider(providerId, userId)
        return ProviderAssembler.toDTO(provider)
    }

    /**
     * 删除服务商
     * @param providerId 服务商id
     * @param userId 用户id
     */
    fun deleteProvider(providerId: String, userId: String) {
        llmDomainService.deleteProvider(providerId, userId, Operator.USER)
    }

    /**
     * 获取用户自己的服务商
     * @param userId 用户id
     * @return List<ProviderDTO>
     **/
    fun getUserProviders(userId: String): List<ProviderDTO> {
        val providers = llmDomainService.getUserProviders(userId)
        return providers.map(ProviderAssembler::toDTO).toList()
    }

    /**
     * 获取所有服务商（包含官方和用户自定义）
     * @param userId 用户ID
     * @return 服务商DTO列表
     */
    fun getAllProviders(userId: String): List<ProviderDTO> {
        val providers = llmDomainService.getAllProviders(userId)
        return providers.map(ProviderAssembler::toDTO).toList()
    }

    /**
     * 获取官方服务商
     * @return 官方服务商DTO列表
     */
    fun getOfficialProviders(): List<ProviderDTO> {
        val providers = llmDomainService.getOfficialProviders()
        return providers.map(ProviderAssembler::toDTO).toList()
    }

    /**
     * 获取用户自定义服务商
     * @param userId 用户ID
     * @return 用户自定义服务商DTO列表
     */
    fun getCustomProviders(userId: String): List<ProviderDTO> {
        val providers = llmDomainService.getCustomProviders(userId)
        return providers.map(ProviderAssembler::toDTO).toList()
    }

    /**
     * 获取用户服务商协议
     * @return List<ProviderProtocol>
    </ProviderProtocol> */
    fun getUserProviderProtocols(): List<ProviderProtocol> {
        return llmDomainService.getProviderProtocols()
    }

    /**
     * 创建模型
     * @param modelCreateRequest 请求对象
     * @param userId 用户id
     * @return ModelDTO
     */
    fun createModel(modelCreateRequest: ModelCreateRequest, userId: String): ModelDTO {
        val model= ModelAssembler.toEntity(modelCreateRequest, userId)
        model.isOfficial=false
        modelCreateRequest.providerId?.let { llmDomainService.checkProviderExists(it, userId) }
        llmDomainService.createModel(model)
        return ModelAssembler.toDTO(model)
    }

    /**
     * 修改模型
     * @param modelUpdateRequest 请求对象
     * @param userId 用户id
     * @return ModelDTO
     */
    fun updateModel(modelUpdateRequest: ModelUpdateRequest, userId: String): ModelDTO {
        val model: ModelEntity = ModelAssembler.toEntity(modelUpdateRequest, userId)
        llmDomainService.updateModel(model)
        return ModelAssembler.toDTO(model)
    }

    /**
     * 删除模型
     * @param modelId 模型id
     * @param userId 用户id
     */
    fun deleteModel(modelId: String, userId: String) {
        llmDomainService.deleteModel(modelId, userId, Operator.ADMIN)
    }

    /**
     * 修改模型状态
     * @param modelId 模型id
     * @param userId 用户id
     */
    fun updateModelStatus(modelId: String, userId: String) {
        llmDomainService.updateModelStatus(modelId, userId)
    }

    /**
     * 根据类型获取服务商
     * @param providerType 服务商类型编码：all-所有，official-官方，user-用户的
     * @param userId 用户ID
     * @return 服务商DTO列表
     */
    fun getProvidersByType(providerType: ProviderType, userId: String): List<ProviderDTO> {
        // 使用枚举常量ProviderType代替硬编码字符串

        val providers = llmDomainService.getProvidersByType(providerType, userId)

        return providers.map(ProviderAssembler::toDTO).toList()
    }

    /**
     * 修改服务商状态
     * @param providerId 服务商id
     * @param userId 用户id
     */
    fun updateProviderStatus(providerId: String, userId: String) {
        llmDomainService.updateProviderStatus(providerId, userId)
    }

    /**
     * 获取所有激活模型
     * @param providerType 服务商类型
     * @param userId 用户id
     * @param modelType 模型类型（可选）
     * @return 模型列表
     */
    fun getActiveModelsByType(
        providerType: ProviderType,
        userId: String,
        modelType: ModelType
    ): List<ModelDTO> {
        return llmDomainService.getProvidersByType(providerType, userId)
            .filter(ProviderAggregate::status)
            .flatMap({ provider -> provider.getModels() })
            .filter({ model -> model.type === modelType })
            .map(ModelAssembler::toDTO)
            .toList()
    }

}