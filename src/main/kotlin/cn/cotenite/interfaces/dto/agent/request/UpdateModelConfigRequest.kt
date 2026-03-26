package cn.cotenite.interfaces.dto.agent.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum

/**
 * 保存模型配置请求对象
 */
data class UpdateModelConfigRequest(
    @field:NotBlank(message = "模型ID不能为空")
    var modelId: String? = null,
    @field:Min(value = 0, message = "temperature最小值为0")
    @field:Max(value = 2, message = "temperature最大值为2")
    var temperature: Double? = null,
    @field:Min(value = 0, message = "topP最小值为0")
    @field:Max(value = 1, message = "topP最大值为1")
    var topP: Double? = null,
    var topK: Int? = null,
    @field:Min(value = 1, message = "maxTokens最小值为1")
    var maxTokens: Int? = null,
    var strategyType: TokenOverflowStrategyEnum = TokenOverflowStrategyEnum.NONE,
    var reserveRatio: Double? = null,
    var summaryThreshold: Int? = null
)
