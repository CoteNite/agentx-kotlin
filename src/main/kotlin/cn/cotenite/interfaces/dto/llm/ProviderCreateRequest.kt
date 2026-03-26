package cn.cotenite.interfaces.dto.llm

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * 服务提供商创建请求
 */
data class ProviderCreateRequest(
    @field:NotNull(message = "协议不能为空")
    var protocol: ProviderProtocol? = null,
    @field:NotBlank(message = "名称不可为空")
    var name: String? = null,
    var description: String? = null,
    var config: ProviderConfig? = null,
    var status: Boolean? = true
)
