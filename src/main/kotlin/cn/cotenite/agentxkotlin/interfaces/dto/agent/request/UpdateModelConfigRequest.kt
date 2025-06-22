package cn.cotenite.agentxkotlin.interfaces.dto.agent.request

import cn.cotenite.agentxkotlin.domain.sahred.enums.TokenOverflowStrategyEnum
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:38
 */
data class UpdateModelConfigRequest(
    /**
     * 模型ID
     */
    @field:NotBlank(message = "模型ID不能为空") // @field: 表示注解应用于字段而非属性的getter/setter
    var modelId: String?, // 允许为null，但通过@NotBlank进行校验

    /**
     * 温度参数，范围0-2
     */
    @field:Min(value = 0, message = "temperature最小值为0")
    @field:Max(value = 2, message = "temperature最大值为2")
    var temperature: Double?,

    /**
     * Top P参数，范围0-1
     */
    @field:Min(value = 0, message = "topP最小值为0")
    @field:Max(value = 1, message = "topP最大值为1")
    var topP: Double?,

    /**
     * topK
     */
    var topK: Int?, // 转换为Kotlin的Int?类型

    /**
     * 最大Token数，适用于滑动窗口和摘要策略
     */
    @field:Min(value = 1, message = "maxTokens最小值为1")
    var maxTokens: Int?, // 转换为Kotlin的Int?类型

    /**
     * 策略类型
     */
    var strategyType: TokenOverflowStrategyEnum = TokenOverflowStrategyEnum.NONE, // 设置默认值

    /**
     * 预留缓冲比例，适用于滑动窗口策略
     * 范围0-1之间的小数，表示预留的空间比例
     */
    var reserveRatio: Double?,

    /**
     * 摘要触发阈值（消息数量），适用于摘要策略
     */
    var summaryThreshold: Int? // 转换为Kotlin的Int?类型
)