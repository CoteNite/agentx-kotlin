package cn.cotenite.interfaces.api.portal.llm

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
import cn.cotenite.application.llm.dto.ModelDTO
import cn.cotenite.application.llm.dto.ProviderDTO
import cn.cotenite.application.llm.service.LLMAppService
import cn.cotenite.domain.llm.model.enums.ModelType
import cn.cotenite.domain.llm.model.enums.ProviderType
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.llm.request.ModelCreateRequest
import cn.cotenite.interfaces.dto.llm.request.ModelUpdateRequest
import cn.cotenite.interfaces.dto.llm.request.ProviderCreateRequest
import cn.cotenite.interfaces.dto.llm.request.ProviderUpdateRequest

/**
 * 大模型服务商控制器
 */
@RestController
@RequestMapping("/llms")
class PortalLLMController(
    private val llmAppService: LLMAppService
) {

    @GetMapping("/providers/{providerId}")
    fun getProviderDetail(@PathVariable providerId: String): Result<ProviderDTO?> =
        Result.success(llmAppService.getProviderDetail(providerId, currentUserId()))

    @GetMapping("/providers")
    fun getProviders(@RequestParam(required = false, defaultValue = "all") type: String): Result<List<ProviderDTO>> =
        Result.success(llmAppService.getProvidersByType(ProviderType.fromCode(type), currentUserId()))

    @PostMapping("/providers")
    fun createProvider(@RequestBody @Validated providerCreateRequest: ProviderCreateRequest): Result<ProviderDTO?> =
        Result.success(llmAppService.createProvider(providerCreateRequest, currentUserId()))

    @PutMapping("/providers")
    fun updateProvider(@RequestBody @Validated providerUpdateRequest: ProviderUpdateRequest): Result<ProviderDTO?> =
        Result.success(llmAppService.updateProvider(providerUpdateRequest, currentUserId()))

    @PostMapping("/providers/{providerId}/status")
    fun updateProviderStatus(@PathVariable providerId: String): Result<Void> {
        llmAppService.updateProviderStatus(providerId, currentUserId())
        return Result.success()
    }

    @DeleteMapping("/providers/{providerId}")
    fun deleteProvider(@PathVariable providerId: String): Result<Void> {
        llmAppService.deleteProvider(providerId, currentUserId())
        return Result.success()
    }

    @GetMapping("/providers/protocols")
    fun getProviderProtocols(): Result<List<ProviderProtocol>> =
        Result.success(llmAppService.getUserProviderProtocols())

    @PostMapping("/models")
    fun createModel(@RequestBody @Validated modelCreateRequest: ModelCreateRequest): Result<ModelDTO?> =
        Result.success(llmAppService.createModel(modelCreateRequest, currentUserId()))

    @PutMapping("/models")
    fun updateModel(@RequestBody @Validated modelUpdateRequest: ModelUpdateRequest): Result<ModelDTO?> =
        Result.success(llmAppService.updateModel(modelUpdateRequest, currentUserId()))

    @DeleteMapping("/models/{modelId}")
    fun deleteModel(@PathVariable modelId: String): Result<Void> {
        llmAppService.deleteModel(modelId, currentUserId())
        return Result.success()
    }

    @PutMapping("/models/{modelId}/status")
    fun updateModelStatus(@PathVariable modelId: String): Result<Void> {
        llmAppService.updateModelStatus(modelId, currentUserId())
        return Result.success()
    }

    @GetMapping("/models/types")
    fun getModelTypes(): Result<List<ModelType>> = Result.success(ModelType.entries)

    @GetMapping("/models/default")
    fun getDefaultModel(): Result<ModelDTO?> =
        Result.success(llmAppService.getDefaultModel(currentUserId()))

    @GetMapping("/models")
    fun getModels(@RequestParam(required = false) modelType: String?): Result<List<ModelDTO>> {
        val type = modelType?.let(ModelType::fromCode)
        return Result.success(llmAppService.getActiveModelsByType(ProviderType.ALL, currentUserId(), type))
    }

    private fun currentUserId(): String = UserContext.getCurrentUserId()
}
