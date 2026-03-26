package cn.cotenite.interfaces.api.admin

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.application.admin.llm.service.AdminLLMAppService
import cn.cotenite.application.llm.dto.ModelDTO
import cn.cotenite.application.llm.dto.ProviderDTO
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.interfaces.dto.llm.ModelUpdateRequest
import cn.cotenite.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.interfaces.dto.llm.ProviderUpdateRequest

/**
 * 管理员LLM管理控制器
 */
@RestController
@RequestMapping("/admin/llm")
class AdminLLMController(
    private val adminLLMAppService: AdminLLMAppService
) {

    @PostMapping("")
    fun createProvider(@RequestBody @Validated request: ProviderCreateRequest): Result<ProviderDTO?> =
        Result.success(adminLLMAppService.createProvider(request, currentUserId()))

    @PutMapping("/{id}")
    fun updateProvider(
        @PathVariable id: String,
        @RequestBody @Validated request: ProviderUpdateRequest
    ): Result<ProviderDTO?> = Result.success(
        adminLLMAppService.updateProvider(request.apply { this.id = id }, currentUserId())
    )

    @DeleteMapping("/{id}")
    fun deleteProvider(@PathVariable id: String): Result<Void> {
        adminLLMAppService.deleteProvider(id, currentUserId())
        return Result.success()
    }

    @PostMapping("/model")
    fun createModel(@RequestBody @Validated request: ModelCreateRequest): Result<ModelDTO?> =
        Result.success(adminLLMAppService.createModel(request, currentUserId()))

    @PutMapping("/model/{id}")
    fun updateModel(
        @PathVariable id: String,
        @RequestBody @Validated request: ModelUpdateRequest
    ): Result<ModelDTO?> = Result.success(
        adminLLMAppService.updateModel(request.apply { this.id = id }, currentUserId())
    )

    @DeleteMapping("/model/{id}")
    fun deleteModel(@PathVariable id: String): Result<Void> {
        adminLLMAppService.deleteModel(id, currentUserId())
        return Result.success()
    }

    private fun currentUserId(): String = UserContext.getCurrentUserId() ?: "admin"
}
