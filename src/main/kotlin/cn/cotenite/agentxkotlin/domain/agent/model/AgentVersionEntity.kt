package cn.cotenite.agentxkotlin.domain.agent.model

import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.domain.agent.dto.AgentVersionDTO
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

/**
 * @Author RichardYoung
 * @Description Agent版本实体类，代表一个Agent的发布版本
 * @Date 2025/6/16 11:44
 *
 * Kotlin 的数据类自动生成 equals(), hashCode(), toString(), copy() 方法，
 * 并且为主构造函数中的属性自动生成 getter/setter (对于 var 属性) 或 getter (对于 val 属性)。
 */
@Entity
@Table(name = "agent_versions")
open class AgentVersionEntity(
    /**
     * 版本唯一ID
     */
    @Id
    @Column(name = "id")
    var id: String? = null,

    /**
     * 关联的Agent ID
     */
    @Column(name = "agent_id", nullable = false)
    var agentId: String,

    /**
     * Agent名称
     */
    @Column(name = "name", nullable = false)
    var name: String,

    /**
     * Agent头像URL
     */
    @Column(name = "avatar")
    var avatar: String? = null,

    /**
     * Agent描述
     */
    @Column(name = "description")
    var description: String? = null,

    /**
     * 版本号，如1.0.0
     */
    @Column(name = "version_number", nullable = false)
    var versionNumber: String,

    /**
     * Agent系统提示词
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
    var modelConfig: ModelConfig? = ModelConfig(),

    /**
     * Agent可使用的工具列表
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tools", columnDefinition = "jsonb")
    var tools: MutableList<AgentTool>? = mutableListOf(),

    /**
     * 关联的知识库ID列表
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "knowledge_base_ids", columnDefinition = "jsonb")
    var knowledgeBaseIds: MutableList<String>? = mutableListOf(),

    /**
     * 版本更新日志
     */
    @Column(name = "change_log")
    var changeLog: String? = null,

    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     */
    @Column(name = "agent_type")
    var agentType: Int? = null,

    /**
     * 发布状态：1-审核中, 2-已发布, 3-拒绝, 4-已下架
     */
    @Column(name = "publish_status")
    var publishStatus: Int? = null,

    /**
     * 审核拒绝原因
     */
    @Column(name = "reject_reason")
    var rejectReason: String? = null,

    /**
     * 审核时间
     */
    @Column(name = "review_time")
    var reviewTime: LocalDateTime? = null,

    /**
     * 发布时间
     */
    @Column(name = "published_at")
    var publishedAt: LocalDateTime? = null,

    /**
     * 创建者用户ID
     */
    @Column(name = "user_id", nullable = false)
    var userId: String,

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
    var multiModal: Boolean? = null,

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

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



    companion object {
        /**
         * 从Agent实体创建一个新的版本实体
         */
        fun createFromAgent(agent: AgentEntity, versionNumber: String, changeLog: String): AgentVersionEntity {
            val now = LocalDateTime.now()
            return AgentVersionEntity(
                agentId = agent.id,
                name = agent.name,
                avatar = agent.avatar,
                description = agent.description,
                versionNumber = versionNumber,
                systemPrompt = agent.systemPrompt,
                welcomeMessage = agent.welcomeMessage,
                modelConfig = agent.modelConfig,
                tools = agent.tools,
                knowledgeBaseIds = agent.knowledgeBaseIds,
                userId = agent.userId,
                agentType = agent.type,
                changeLog = changeLog,
                createdAt = now,
                updatedAt = now,
                publishedAt = now,
                publishStatus = PublishStatus.REVIEWING.code,
                reviewTime = now
            )
        }
    }

    fun toDTO(): AgentVersionDTO {
        return AgentVersionDTO(
            id = id,
            agentId = agentId,
            name = name,
            avatar = avatar,
            description = description,
            versionNumber = versionNumber,
            systemPrompt = systemPrompt,
            welcomeMessage = welcomeMessage,
            modelConfig = modelConfig,
            tools = tools,
            knowledgeBaseIds = knowledgeBaseIds,
            changeLog = changeLog,
            agentType = agentType,
            publishStatus = publishStatus,
            rejectReason = rejectReason,
            reviewTime = reviewTime,
            publishedAt = publishedAt,
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt
        )
    }


    /**
     * 更新发布状态
     */
    fun updatePublishStatus(status: PublishStatus) {
        this.publishStatus = status.code
        this.reviewTime = LocalDateTime.now()
    }

    /**
     * 拒绝发布
     */
    fun reject(reason: String?) {
        this.publishStatus = PublishStatus.REJECTED.code
        this.rejectReason = reason
        this.reviewTime = LocalDateTime.now()
    }


}
