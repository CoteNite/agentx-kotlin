package cn.cotenite.agentxkotlin.application.admin.llm.service

import cn.cotenite.agentxkotlin.application.llm.assembler.ModelAssembler
import cn.cotenite.agentxkotlin.application.llm.assembler.ProviderAssembler
import cn.cotenite.agentxkotlin.application.llm.dto.ModelDTO
import cn.cotenite.agentxkotlin.application.llm.dto.ProviderDTO
import cn.cotenite.agentxkotlin.domain.llm.service.LlmDomainService
import cn.cotenite.agentxkotlin.infrastructure.entity.Operator
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelUpdateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderUpdateRequest
import org.springframework.stereotype.Service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:24
 */
@Service
class AdminLLMAppService(
    private val llmDomainService: LlmDomainService
){
    
    /**
     * 創建官方服務商
     * @param providerCreateRequest 請求物件
     * @param userId 用戶ID
     */
    fun createProvider(providerCreateRequest: ProviderCreateRequest, userId: String): ProviderDTO? {
        val provider = ProviderAssembler.toEntity(providerCreateRequest, userId)
        provider.isOfficial = true // 直接訪問屬性
        return ProviderAssembler.toDTO(llmDomainService.createProvider(provider))
    }

    /**
     * 修改服務商
     * @param providerUpdateRequest 請求物件
     * @param userId 用戶ID
     */
    fun updateProvider(providerUpdateRequest: ProviderUpdateRequest, userId: String): ProviderDTO? {
        val provider = ProviderAssembler.toEntity(providerUpdateRequest, userId)
        provider.setAdmin() // 假設 setAdmin() 是 ProviderEntity 的方法
        llmDomainService.updateProvider(provider)
        return null // 原始 Java 代碼返回 null，保持一致
    }

    /**
     * 刪除服務商
     * @param providerId 服務商ID
     * @param userId 用戶ID
     */
    fun deleteProvider(providerId: String, userId: String) {
        llmDomainService.deleteProvider(providerId, userId, Operator.ADMIN)
    }

    /**
     * 創建模型
     * @param modelCreateRequest 模型物件
     * @param userId 用戶ID
     */
    fun createModel(modelCreateRequest: ModelCreateRequest, userId: String): ModelDTO? {
        val entity = ModelAssembler.toEntity(modelCreateRequest, userId)
        entity.setAdmin()
        entity.isOfficial = true // 直接訪問屬性
        llmDomainService.createModel(entity)
        return ModelAssembler.toDTO(entity)
    }

    /**
     * 更新模型
     * @param modelUpdateRequest 模型請求物件
     * @param userId 用戶ID
     */
    fun updateModel(modelUpdateRequest: ModelUpdateRequest, userId: String): ModelDTO? {
        val entity = ModelAssembler.toEntity(modelUpdateRequest, userId)
        entity.setAdmin()
        llmDomainService.updateModel(entity)
        return ModelAssembler.toDTO(entity)
    }

    /**
     * 刪除模型
     * @param modelId 模型ID
     * @param userId 用戶ID
     */
    fun deleteModel(modelId: String, userId: String) {
        llmDomainService.deleteModel(modelId, userId, Operator.ADMIN)
    }
}