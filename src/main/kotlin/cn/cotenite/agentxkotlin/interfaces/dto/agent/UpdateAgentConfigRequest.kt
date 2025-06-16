package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import cn.cotenite.agentxkotlin.domain.agent.model.ModelConfig
import cn.cotenite.agentxkotlin.domain.common.util.ValidationUtils

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 14:06
 */
data class UpdateAgentConfigRequest(
    val systemPrompt:String,
    val welcomeMessage:String,
    val modelConfig: ModelConfig,
    val tools:MutableList<AgentTool>,
    val knowledgeBaseIds:MutableList<String>
){

    fun validate() {
        ValidationUtils.notEmpty(systemPrompt, "systemPrompt")
    }

}
