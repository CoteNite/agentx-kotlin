package cn.cotenite.agentxkotlin.domain.llm.model.config

import cn.cotenite.agentxkotlin.domain.sahred.enums.TokenOverflowStrategyEnum
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:04
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略值为 null 的字段
data class LLMModelConfig(
    /**
     * 模型id
     */
    var modelId: String? = null,

    /**
     * 温度参数，范围0-2，值越大创造性越强，越小则越保守
     */
    var temperature: Double = 0.7, // 使用默认参数

    /**
     * Top P参数，范围0-1，控制输出的多样性
     */
    var topP: Double = 0.7, // 使用默认参数

    var topK: Int  = 50,

    /**
     * 最大Token数，适用于滑动窗口和摘要策略
     */
    var maxTokens: Int? = null, // Int? 表示可空

    /**
     * 策略类型 @link TokenOverflowStrategyEnum
     */
    var strategyType: TokenOverflowStrategyEnum? = null,

    /**
     * 预留缓冲比例，适用于滑动窗口策略
     * 范围0-1之间的小数，表示预留的空间比例
     */
    var reserveRatio: Double? = null,

    /**
     * 摘要触发阈值（消息数量），适用于摘要策略
     */
    var summaryThreshold: Int? = null
) {

}