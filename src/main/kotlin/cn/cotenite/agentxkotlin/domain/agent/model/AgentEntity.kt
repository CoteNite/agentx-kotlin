package cn.cotenite.agentxkotlin.domain.agent.model

import cn.cotenite.agentxkotlin.domain.agent.dto.AgentDTO
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

/**
 * @Author RichardYoung
 * @Description Agent实体类
 * @Date 2025/6/16 03:01
 */
@Entity
@Table(name = "agents")
open class AgentEntity(
    /**
     * Agent 唯一ID
     */
    @Id
    @Column(name = "id")
    var id: String = UUID.randomUUID().toString(),

    /**
     * Agent 名称
     */
    @Column(name = "name", nullable = false)
    var name: String,

    /**
     * Agent 头像URL
     */
    @Column(name = "avatar")
    var avatar: String? = null,

    /**
     * Agent 描述
     */
    @Column(name = "description")
    var description: String? = null,

    /**
     * Agent 系统提示词
     */
    @Column(name = "system_prompt")
    var systemPrompt: String? = null,

    /**
     * 欢迎消息
     */
    @Column(name = "welcome_message")
    var welcomeMessage: String? = null,

    /**
     * 模型配置，包含模型类型、温度等参数
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "model_config", columnDefinition = "jsonb")
    var modelConfig: ModelConfig? = null,

    /**
     * Agent 可使用的工具列表
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tools", columnDefinition = "jsonb")
    var tools: MutableList<AgentTool>? = null,

    /**
     * 关联的知识库ID列表
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "knowledge_base_ids", columnDefinition = "jsonb")
    var knowledgeBaseIds: MutableList<String>? = null,

    /**
     * 已发布的版本号ID
     */
    @Column(name = "published_version")
    var publishedVersion: String? = null,

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    var enabled: Boolean = true,

    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     */
    @Column(name = "agent_type")
    var type: Int = 1,

    /**
     * 创建者用户ID
     */
    @Column(name = "user_id", nullable = false)
    val userId: String,

    /**
     * 工具预设参数
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_preset_params", columnDefinition = "jsonb")
    var toolPresetParams: String? = null,

    /**
     * 多模态能力
     */
    @Column(name = "multi_modal")
    var multiModal: Boolean = false,

    /**
     * 标签列表
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    var tags: MutableList<String>? = null,

    /**
     * 是否公开
     */
    @Column(name = "is_public")
    var isPublic: Boolean = false,

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 最后更新时间
     */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 删除时间（软删除）
     */
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
) {
    fun toDTO(): AgentDTO {
        return AgentDTO(
            id = this.id,
            name = this.name,
            avatar = this.avatar,
            description = this.description,
            systemPrompt = this.systemPrompt,
            welcomeMessage = this.welcomeMessage,
            modelConfig = this.modelConfig,
            tools = this.tools,
            knowledgeBaseIds = this.knowledgeBaseIds,
            publishedVersion = this.publishedVersion,
            enabled = this.enabled,
            agentType = this.type,
            userId = this.userId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }


    /**
     * 启用Agent
     */
    fun enable() {
        this.enabled = true
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 禁用Agent
     */
    fun disable() {
        this.enabled = false
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 发布新版本
     */
    fun publishVersion(versionId: String) {
        this.publishedVersion = versionId
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 软删除
     */
    fun delete() {
        this.deletedAt = LocalDateTime.now()
    }
}
