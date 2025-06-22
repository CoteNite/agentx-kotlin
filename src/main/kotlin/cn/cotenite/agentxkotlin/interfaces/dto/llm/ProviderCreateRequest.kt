package cn.cotenite.agentxkotlin.interfaces.dto.llm

import cn.cotenite.agentxkotlin.domain.llm.model.config.ProviderConfig
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

/**
 * 服务提供商创建请求
 */
data class ProviderCreateRequest(
    /**
     * 服务商协议
     */
    @field:NotNull(message = "协议不能为空")
    var protocol: ProviderProtocol? = null,

    /**
     * 服务商名称
     */
    @field:NotBlank(message = "名称不可为空")
    var name: String? = null,

    /**
     * 服务商描述
     */
    var description: String? = null,

    /**
     * 服务商配置
     */
    var config: ProviderConfig? = null,

    /**
     * 服务商状态
     */
    var status: Boolean = true
)