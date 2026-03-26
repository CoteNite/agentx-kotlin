package cn.cotenite.domain.agent.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import cn.cotenite.domain.agent.constant.AgentType
import cn.cotenite.infrastructure.entity.BaseEntity
import java.time.LocalDateTime

/**
 * Agent实体
 */
@TableName(value = "agents", autoResultMap = true)
class AgentEntity : BaseEntity() {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null
    @TableField("name")
    var name: String? = null
    @TableField("avatar")
    var avatar: String? = null
    @TableField("description")
    var description: String? = null
    @TableField("system_prompt")
    var systemPrompt: String? = null
    @TableField("welcome_message")
    var welcomeMessage: String? = null
    @TableField(value = "tools", exist = false)
    var tools: MutableList<AgentTool> = mutableListOf()
    @TableField(value = "knowledge_base_ids", exist = false)
    var knowledgeBaseIds: MutableList<String> = mutableListOf()
    @TableField("published_version")
    var publishedVersion: String? = null
    @TableField("enabled")
    var enabled: Boolean = true
    @TableField("agent_type")
    var agentType: Int = AgentType.CHAT_ASSISTANT.code
    @TableField("user_id")
    var userId: String? = null

    companion object {
        fun createNew(
            name: String?,
            description: String?,
            avatar: String?,
            agentType: Int?,
            userId: String?
        ): AgentEntity {
            val now = LocalDateTime.now()
            return AgentEntity().apply {
                this.name = name
                this.description = description
                this.avatar = avatar
                this.agentType = agentType ?: AgentType.CHAT_ASSISTANT.code
                this.userId = userId
                enabled = true
                createdAt = now
                updatedAt = now
            }
        }
    }

    fun updateBasicInfo(name: String?, avatar: String?, description: String?) {
        this.name = name
        this.avatar = avatar
        this.description = description
        updatedAt = LocalDateTime.now()
    }

    fun updateConfig(
        systemPrompt: String?,
        welcomeMessage: String?,
        tools: List<AgentTool>?,
        knowledgeBaseIds: List<String>?
    ) {
        this.systemPrompt = systemPrompt
        this.welcomeMessage = welcomeMessage
        this.tools = tools?.toMutableList() ?: mutableListOf()
        this.knowledgeBaseIds = knowledgeBaseIds?.toMutableList() ?: mutableListOf()
        updatedAt = LocalDateTime.now()
    }

    fun enable() = run {
        enabled = true
        updatedAt = LocalDateTime.now()
    }

    fun disable() = run {
        enabled = false
        updatedAt = LocalDateTime.now()
    }

    fun publishVersion(versionId: String?) = run {
        publishedVersion = versionId
        updatedAt = LocalDateTime.now()
    }

    fun delete() {
        deletedAt = LocalDateTime.now()
    }

    fun getAgentTypeEnum(): AgentType = AgentType.fromCode(agentType)
}
