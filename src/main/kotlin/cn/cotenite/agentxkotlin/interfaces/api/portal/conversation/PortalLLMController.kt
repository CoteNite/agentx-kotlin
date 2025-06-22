package cn.cotenite.agentxkotlin.interfaces.api.portal.conversation

import cn.cotenite.agentxkotlin.application.llm.dto.ModelDTO
import cn.cotenite.agentxkotlin.application.llm.dto.ProviderDTO
import cn.cotenite.agentxkotlin.application.llm.service.LLMAppService
import cn.cotenite.agentxkotlin.domain.llm.model.enums.ModelType
import cn.cotenite.agentxkotlin.domain.llm.model.enums.ProviderType
import cn.cotenite.agentxkotlin.infrastructure.auth.UserContext
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelUpdateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderUpdateRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:33
 */
@RestController
@RequestMapping("/llm")
class PortalLLMController (
    private val llmAppService: LLMAppService // 通過構造函數注入 LLMAppService
) {

    /**
     * 獲取服務商詳細資訊
     * @param providerId 服務商ID
     */
    @GetMapping("/providers/{providerId}")
    fun getProviderDetail(@PathVariable providerId: String): Response<ProviderDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(llmAppService.getProviderDetail(providerId, userId))
    }

    /**
     * 獲取服務商列表，支持按類型過濾
     * @param type 服務商類型：all-所有，official-官方，user-用戶的（默認）
     * @return 服務商列表
     */
    @GetMapping("/providers")
    fun getProviders(
        @RequestParam(required = false, defaultValue = "all") type: String
    ): Response<List<ProviderDTO>> {
        val providerType = ProviderType.fromCode(type)?:throw BusinessException("Invalid provider type")
        val userId = UserContext.getCurrentUserId()
        return Response.success(llmAppService.getProvidersByType(providerType, userId))
    }

    /**
     * 創建服務提供商
     * @param providerCreateRequest 服務提供商創建請求
     */
    @PostMapping("/providers")
    fun createProvider(@RequestBody providerCreateRequest: ProviderCreateRequest): Response<ProviderDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(llmAppService.createProvider(providerCreateRequest, userId))
    }

    /**
     * 更新服務提供商
     * @param providerUpdateRequest 服務提供商更新請求
     */
    @PutMapping("/providers")
    fun updateProvider(@RequestBody providerUpdateRequest: ProviderUpdateRequest): Response<ProviderDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(llmAppService.updateProvider(providerUpdateRequest, userId))
    }

    /**
     * 修改服務商狀態
     * @param providerId 服務商ID
     */
    @PostMapping("/providers/{providerId}/status")
    fun updateProviderStatus(@PathVariable providerId: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        llmAppService.updateProviderStatus(providerId, userId)
        return Response.success()
    }

    /**
     * 刪除服務提供商
     * @param providerId 服務提供商ID
     */
    @DeleteMapping("/providers/{providerId}")
    fun deleteProvider(@PathVariable providerId: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        llmAppService.deleteProvider(providerId, userId)
        return Response.success()
    }

    /**
     * 獲取服務提供商協議列表
     */
    @GetMapping("/providers/protocols")
    fun getProviderProtocols(): Response<List<ProviderProtocol>> { // 方法名避免與 getProviders 衝突
        return Response.success(llmAppService.getUserProviderProtocols())
    }

    /**
     * 添加模型
     * @param modelCreateRequest ModelCreateRequest
     */
    @PostMapping("/models")
    fun createModel(@RequestBody modelCreateRequest: ModelCreateRequest): Response<ModelDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(llmAppService.createModel(modelCreateRequest, userId))
    }

    /**
     * 修改模型
     * @param modelUpdateRequest ModelUpdateRequest
     */
    @PutMapping("/models")
    fun updateModel(@RequestBody @Validated modelUpdateRequest: ModelUpdateRequest): Response<ModelDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(llmAppService.updateModel(modelUpdateRequest, userId))
    }

    /**
     * 刪除模型
     * @param modelId 模型主鍵
     */
    @DeleteMapping("/models/{modelId}")
    fun deleteModel(@PathVariable modelId: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        llmAppService.deleteModel(modelId, userId)
        return Response.success()
    }

    /**
     * 修改模型狀態
     * @param modelId 模型主鍵
     */
    @PutMapping("/models/{modelId}/status")
    fun updateModelStatus(@PathVariable modelId: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        llmAppService.updateModelStatus(modelId, userId)
        return Response.success()
    }

    /**
     * 獲取模型類型
     * @return
     */
    @GetMapping("/models/types")
    fun getModelTypes(): Response<List<ModelType>> {
        // Kotlin 中獲取枚舉所有值的慣用方法是 .entries
        return Response.success(ModelType.entries)
    }

    /**
     * 獲取所有激活模型
     * @param modelType 模型類型（可選），不傳則查詢所有類型
     * @return 模型列表
     */
    @GetMapping("/models")
    fun getModels(@RequestParam(required = false) modelType: String?): Response<List<ModelDTO>> {
        val userId = UserContext.getCurrentUserId()
        val type = modelType?.let { ModelType.fromCode(it) }?:throw BusinessException("modelType is required")
        return Response.success(llmAppService.getActiveModelsByType(ProviderType.ALL, userId, type))
    }
}