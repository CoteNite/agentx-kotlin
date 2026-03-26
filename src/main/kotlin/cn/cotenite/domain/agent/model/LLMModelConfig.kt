package cn.cotenite.domain.agent.model

import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum

/**
 * LLM模型配置
 */
data class LLMModelConfig(
    /**
     * 模型ID
     */
    var modelId: String? = null,
    /**
     * 温度
     */
    var temperature: Double? = null,
    /**
     * topP
     */
    var topP: Double? = null,
    /**
     * topK
     */
    var topK: Int? = null,
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
