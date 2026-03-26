package cn.cotenite.domain.agent.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import cn.cotenite.domain.agent.constant.PublishStatus
import cn.cotenite.infrastructure.converter.ListConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import java.time.LocalDateTime

/**
 * Agent版本实体
 */
@TableName(value = "agent_versions", autoResultMap = true)
class AgentVersionEntity : BaseEntity() {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null
    @TableField("agent_id")
    var agentId: String? = null
    @TableField("name")
    var name: String? = null
    @TableField("avatar")
    var avatar: String? = null
    @TableField("description")
    var description: String? = null
    @TableField("version_number")
    var versionNumber: String? = null
    @TableField("system_prompt")
    var systemPrompt: String? = null
    @TableField("welcome_message")
    var welcomeMessage: String? = null
    @TableField(value = "tools", typeHandler = ListConverter::class)
    var tools: MutableList<AgentTool> = mutableListOf()
    @TableField(value = "knowledge_base_ids", typeHandler = ListConverter::class)
    var knowledgeBaseIds: MutableList<String> = mutableListOf()
    @TableField("change_log")
    var changeLog: String? = null
    @TableField("agent_type")
    var agentType: Int? = null
    @TableField("publish_status")
    var publishStatus: Int = PublishStatus.REVIEWING.code
    @TableField("reject_reason")
    var rejectReason: String? = null
    @TableField("review_time")
    var reviewTime: LocalDateTime? = null
    @TableField("published_at")
    var publishedAt: LocalDateTime? = null
    @TableField("user_id")
    var userId: String? = null

    fun getPublishStatusEnum(): PublishStatus = PublishStatus.fromCode(publishStatus)

    fun updatePublishStatus(status: PublishStatus) = run {
        publishStatus = status.code
        reviewTime = LocalDateTime.now()
    }

    fun reject(reason: String?) = run {
        publishStatus = PublishStatus.REJECTED.code
        rejectReason = reason
        reviewTime = LocalDateTime.now()
    }

    companion object {
        fun createFromAgent(agent: AgentEntity, versionNumber: String?, changeLog: String?): AgentVersionEntity {
            val now = LocalDateTime.now()
            return AgentVersionEntity().apply {
                agentId = agent.id
                name = agent.name
                avatar = agent.avatar
                description = agent.description
                this.versionNumber = versionNumber
                systemPrompt = agent.systemPrompt
                welcomeMessage = agent.welcomeMessage
                tools = agent.tools
                knowledgeBaseIds = agent.knowledgeBaseIds
                this.changeLog = changeLog
                agentType = agent.agentType
                userId = agent.userId
                createdAt = now
                updatedAt = now
                publishedAt = now
                publishStatus = PublishStatus.REVIEWING.code
                reviewTime = now
            }
        }
    }
}
