package cn.cotenite.agentxkotlin.interfaces.dto.llm

import cn.cotenite.agentxkotlin.domain.llm.model.config.LLMModelConfig
import jakarta.validation.constraints.NotBlank

/**
 * 模型更新请求
 */
data class ModelUpdateRequest(
    /**
     * 模型ID
     */
    var id: String? = null,
    
    /**
     * 模型id
     */
    @field:NotBlank(message = "模型id不可为空")
    var modelId: String? = null,
    
    /**
     * 模型名称
     */
    @field:NotBlank(message = "名称不可为空")
    var name: String? = null,
    
    /**
     * 模型描述
     */
    var description: String? = null,
    
    /**
     * 模型配置
     */
    var config: LLMModelConfig? = null
)