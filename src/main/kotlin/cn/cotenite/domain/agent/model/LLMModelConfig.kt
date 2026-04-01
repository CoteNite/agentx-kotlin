package cn.cotenite.domain.agent.model

import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * LLM模型配置
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class LLMModelConfig(
    /**
     * 模型ID
     */
    var modelId: String? = null,
    /**
     * 温度
     */
    var temperature: Double = 0.7,
    /**
     * topP
     */
    var topP: Double = 0.7,
    /**
     * topK
     */
    var topK: Int = 50,
    /**
     * 最大token
     */
    var maxTokens: Int? = null,
    /**
     * token溢出策略
     */
    var strategyType: TokenOverflowStrategyEnum = TokenOverflowStrategyEnum.NONE,
    /**
     * 预留比例
     */
    var reserveRatio: Double? = null,
    /**
     * 摘要阈值
     */
    var summaryThreshold: Int? = null
)
