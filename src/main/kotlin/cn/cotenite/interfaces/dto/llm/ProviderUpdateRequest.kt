package cn.cotenite.interfaces.dto.llm

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * 服务提供商更新请求
 */
data class ProviderUpdateRequest(
    var id: String? = null,
    var description: String? = null,
    @field:NotNull(message = "协议不可为空")
    var protocol: ProviderProtocol? = null,
    @field:NotBlank(message = "名称不可为空")
    var name: String? = null,
    var config: ProviderConfig? = null,
    var status: Boolean? = null
)
