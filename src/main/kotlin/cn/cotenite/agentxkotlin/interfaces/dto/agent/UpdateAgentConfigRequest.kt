package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.agent.model.AgentModelConfig
import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import cn.cotenite.agentxkotlin.infrastructure.util.ValidationUtils

/**
 * 更新Agent配置的请求对象
 */
data class UpdateAgentConfigRequest(
    var systemPrompt: String? = null,
    var welcomeMessage: String? = null,
    var modelConfig: AgentModelConfig? = null,
    var tools: List<AgentTool>? = null,
    var knowledgeBaseIds: List<String>? = null
) {
    /**
     * 校验请求参数
     */
    fun validate() {
        ValidationUtils.notEmpty(systemPrompt, "systemPrompt")
        // 其他字段可以为空，不做强制校验
    }
}