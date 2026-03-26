package cn.cotenite.application.agent.assembler

import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.interfaces.dto.agent.request.UpdateModelConfigRequest

/**
 * Agent工作区对象组装器
 */
object AgentWorkspaceAssembler {

    fun toLLMModelConfig(request: UpdateModelConfigRequest): LLMModelConfig =
        LLMModelConfig(
            modelId = request.modelId,
            temperature = request.temperature,
            topP = request.topP,
            topK = request.topK,
            maxTokens = request.maxTokens,
            strategyType = request.strategyType,
            reserveRatio = request.reserveRatio,
            summaryThreshold = request.summaryThreshold
        )
}
