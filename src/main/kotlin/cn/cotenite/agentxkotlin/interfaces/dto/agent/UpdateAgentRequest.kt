package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import cn.cotenite.agentxkotlin.domain.agent.model.ModelConfig
import cn.cotenite.agentxkotlin.domain.common.util.ValidationUtils

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 14:07
 */
data class UpdateAgentRequest(
    val name: String,
    val avatar: String,
    val description: String,
    val systemPrompt: String,
    val welcomeMessage: String,
    val modelConfig: ModelConfig,
    val tools: MutableList<AgentTool>,
    val knowledgeBaseIds: MutableList<String>
){

    /**
     * 校验请求参数
     */
    fun validate() {
        // 必填字段校验
        ValidationUtils.notEmpty(name, "name")
        ValidationUtils.length(name, 1, 50, "name")
    }
}
