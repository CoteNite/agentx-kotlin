package cn.cotenite.agentxkotlin.interfaces.api.admin

import cn.cotenite.agentxkotlin.application.admin.llm.service.AdminLLMAppService
import cn.cotenite.agentxkotlin.application.llm.dto.ModelDTO
import cn.cotenite.agentxkotlin.application.llm.dto.ProviderDTO
import cn.cotenite.agentxkotlin.infrastructure.auth.UserContext
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ModelUpdateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderCreateRequest
import cn.cotenite.agentxkotlin.interfaces.dto.llm.ProviderUpdateRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:31
 */
@RestController // 標註為 REST 控制器
@RequestMapping("/admin/llm") // 設定基礎請求路徑
class AdminLLMController(
    private val adminLLMAppService: AdminLLMAppService
) {

    /**
     * 創建服務商
     * @param request 請求物件
     */
    @PostMapping("")
    fun createProvider(@RequestBody @Validated request: ProviderCreateRequest): Response<ProviderDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(adminLLMAppService.createProvider(request, userId))
    }

    /**
     * 更新服務商
     * @param id 服務商ID
     * @param request 請求物件
     */
    @PutMapping("/{id}")
    fun updateProvider(@PathVariable id: String, @RequestBody @Validated request: ProviderUpdateRequest): Response<ProviderDTO?> {
        val userId = UserContext.getCurrentUserId()
        request.id = id // 直接訪問屬性
        return Response.success(adminLLMAppService.updateProvider(request, userId))
    }

    /**
     * 刪除服務商
     * @param id 服務商ID
     */
    @DeleteMapping("/{id}")
    fun deleteProvider(@PathVariable id: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        adminLLMAppService.deleteProvider(id, userId)
        return Response.success()
    }

    /**
     * 創建模型
     * @param request 請求物件
     */
    @PostMapping("/model")
    fun createModel(@RequestBody @Validated request: ModelCreateRequest): Response<ModelDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(adminLLMAppService.createModel(request, userId))
    }

    /**
     * 更新模型
     * @param id 更新的ID
     * @param request 請求物件
     */
    @PutMapping("/model/{id}")
    fun updateModel(@PathVariable id: String, @RequestBody @Validated request: ModelUpdateRequest): Response<ModelDTO?> {
        val userId = UserContext.getCurrentUserId()
        request.id = id // 直接訪問屬性
        return Response.success(adminLLMAppService.updateModel(request, userId))
    }

    /**
     * 刪除模型
     * @param id 模型ID
     */
    @DeleteMapping("/model/{id}")
    fun deleteModel(@PathVariable id: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        adminLLMAppService.deleteModel(id, userId)
        return Response.success()
    }
}