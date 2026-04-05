package cn.cotenite.application.agent.dto

import cn.cotenite.domain.agent.constant.AgentType
import cn.cotenite.domain.agent.constant.PublishStatus
import cn.cotenite.domain.agent.model.AgentTool
import java.time.LocalDateTime

/**
 * Agent版本数据传输对象
 */
data class AgentVersionDTO(
    var id: String? = null,
    var agentId: String? = null,
    var name: String? = null,
    var avatar: String? = null,
    var description: String? = null,
    var versionNumber: String? = null,
    var systemPrompt: String? = null,
    var welcomeMessage: String? = null,
    var tools: List<AgentTool> = emptyList(),
    var knowledgeBaseIds: List<String> = emptyList(),
    var changeLog: String? = null,
    var agentType: Int? = null,
    var publishStatus: Int? = null,
    var rejectReason: String? = null,
    var reviewTime: LocalDateTime? = null,
    var publishedAt: LocalDateTime? = null,
    var userId: String? = null,
    var addWorkspace: Boolean? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
) {
    /**
     * 获取类型文本描述
     */
    fun getAgentTypeText(): String = AgentType.fromCode(agentType).description

    /**
     * 获取发布状态描述
     */
    fun getPublishStatusText(): String = PublishStatus.fromCode(publishStatus).description
}
