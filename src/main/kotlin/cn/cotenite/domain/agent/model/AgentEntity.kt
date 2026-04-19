package cn.cotenite.domain.agent.model

import cn.cotenite.infrastructure.converter.ListStringConverter
import cn.cotenite.infrastructure.converter.MapConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
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
    /** Agent可使用的工具列表  */
    @TableField(value = "tool_ids", typeHandler = ListStringConverter::class)
    var toolIds: MutableList<String> =mutableListOf()
    @TableField(value = "knowledge_base_ids", exist = false)
    var knowledgeBaseIds: MutableList<String> = mutableListOf()
    @TableField("published_version")
    var publishedVersion: String? = null
    @TableField("enabled")
    var enabled: Boolean = true
    /** Agent类型：1-聊天助手, 2-功能性Agent */
    @TableField("agent_type")
    var agentType: Int = 1
    /** 是否支持多模态  */
    @TableField("multi_modal")
    var multiModal: Boolean? = null

    @TableField("user_id")
    var userId: String? = null

    /** 预先设置工具参数，结构如下： { "<mcpServerName>":{ "toolName":"paranms" } } </mcpServerName> */
    @TableField(value = "tool_preset_params", typeHandler = MapConverter::class)
    var toolPresetParams: MutableMap<String?, MutableMap<String?, MutableMap<String?, String?>?>?>? = null


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
                this.agentType = agentType ?: 1
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


}
