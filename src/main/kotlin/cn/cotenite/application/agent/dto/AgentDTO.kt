package cn.cotenite.application.agent.dto

import cn.cotenite.domain.agent.constant.AgentStatus
import cn.cotenite.domain.agent.constant.AgentType
import cn.cotenite.domain.agent.model.AgentTool
import java.time.LocalDateTime

/**
 * Agent数据传输对象
 */
data class AgentDTO(
    var id: String? = null,
    var name: String? = null,
    var avatar: String? = null,
    var description: String? = null,
    var systemPrompt: String? = null,
    var welcomeMessage: String? = null,
    var toolIds: List<String> = emptyList(),
    var knowledgeBaseIds: List<String> = emptyList(),
    var publishedVersion: String? = null,
    var enabled: Boolean = true,
    var agentType: Int? = null,
    var userId: String? = null,
    var createdAt: LocalDateTime? = null,
    var multiModal: Boolean?=true,
    var updatedAt: LocalDateTime? = null
) {
    /**
     * 获取状态文本描述
     */
    fun getStatusText(): String = AgentStatus.fromCode(if (enabled) 1 else 0).description

    /**
     * 获取类型文本描述
     */
    fun getAgentTypeText(): String = AgentType.fromCode(agentType).description
}
