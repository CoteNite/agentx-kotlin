package cn.cotenite.interfaces.dto.agent

import cn.cotenite.domain.agent.model.AgentTool

/**
 * 更新Agent配置请求
 */
data class UpdateAgentConfigRequest(
    var systemPrompt: String? = null,
    var welcomeMessage: String? = null,
    var tools: List<AgentTool>? = null,
    var knowledgeBaseIds: List<String>? = null
)
