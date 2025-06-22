package cn.cotenite.agentxkotlin.application.agent.assembler

import cn.cotenite.agentxkotlin.domain.llm.model.config.LLMModelConfig
import cn.cotenite.agentxkotlin.interfaces.dto.agent.request.UpdateModelConfigRequest
import org.springframework.beans.BeanUtils


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:39
 */
object AgentWorkspaceAssembler {

    fun toLLMModelConfig(request: UpdateModelConfigRequest): LLMModelConfig {
        val llmModelConfig = LLMModelConfig()
        BeanUtils.copyProperties(request, llmModelConfig)
        return llmModelConfig
    }


}