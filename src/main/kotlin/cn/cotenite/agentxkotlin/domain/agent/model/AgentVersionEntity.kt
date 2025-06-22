package cn.cotenite.agentxkotlin.domain.agent.model

import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.domain.agent.constant.AgentType
import cn.cotenite.agentxkotlin.infrastructure.converter.AgentModelConfigConverter
import cn.cotenite.agentxkotlin.infrastructure.converter.ListConverter
import cn.cotenite.agentxkotlin.infrastructure.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

/**
 * @Author RichardYoung
 * @Description Agent版本实体类，代表一个Agent的发布版本
 * @Date 2025/6/16 11:44
 *
 */
@Entity
@Table(name = "agent_versions")
open
class AgentVersionEntity(

    /**
     * 版本唯一ID
     */
    @field:Id // 标记为主键
    @field:GeneratedValue(strategy = GenerationType.UUID) // 使用UUID作为ID生成策略
    @field:Column(name = "id", nullable = false, updatable = false) // 明确列名和属性
    var id: String? = null,

    /**
     * 关联的Agent ID
     */
    @field:Column(name = "agent_id", nullable = false)
    var agentId: String? = null,

    /**
     * Agent名称
     */
    @field:Column(name = "name")
    var name: String? = null,

    /**
     * Agent头像URL
     */
    @field:Column(name = "avatar")
    var avatar: String? = null,

    /**
     * Agent描述
     */
    @field:Column(name = "description", length = 512)
    var description: String? = null,

    /**
     * 版本号，如1.0.0
     */
    @field:Column(name = "version_number", nullable = false)
    var versionNumber: String? = null,

    /**
     * Agent系统提示词
     */
    @field:Column(name = "system_prompt", length = 2048)
    var systemPrompt: String? = null,

    /**
     * 欢迎消息
     */
    @field:Column(name = "welcome_message")
    var welcomeMessage: String? = null,

    /**
     * 模型配置，包含模型类型、温度等参数
     */
    @field:Column(name = "model_config", columnDefinition = "json")
    @Convert(converter = AgentModelConfigConverter::class)
    var modelConfig: AgentModelConfig? = AgentModelConfig.createDefault(),

    /**
     * Agent可使用的工具列表
     */
    @field:Column(name = "tools", columnDefinition = "json")
    @Convert(converter = ListConverter::class)
    var tools: MutableList<AgentTool>? = mutableListOf(),

    /**
     * 关联的知识库ID列表
     */
    @field:Column(name = "knowledge_base_ids", columnDefinition = "json")
    @Convert(converter = ListConverter::class)
    var knowledgeBaseIds: MutableList<String>? = mutableListOf(),

    /**
     * 版本更新日志
     */
    @field:Column(name = "change_log", length = 2048)
    var changeLog: String? = null,

    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     */
    @field:Column(name = "agent_type", nullable = false)
    var agentType: Int = AgentType.CHAT_ASSISTANT.code, // 提供默认值

    /**
     * 发布状态：1-审核中, 2-已发布, 3-拒绝, 4-已下架
     */
    @field:Column(name = "publish_status", nullable = false)
    var publishStatus: Int = PublishStatus.REVIEWING.code, // 提供默认值

    /**
     * 审核拒绝原因
     */
    @field:Column(name = "reject_reason", length = 512)
    var rejectReason: String? = null,

    /**
     * 审核时间
     */
    @field:Column(name = "review_time")
    var reviewTime: LocalDateTime? = null,

    /**
     * 发布时间
     */
    @field:Column(name = "published_at")
    var publishedAt: LocalDateTime? = null,

    /**
     * 创建者用户ID
     */
    @field:Column(name = "user_id", nullable = false)
    var userId: String? = null

) : BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as AgentVersionEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "AgentVersionEntity(id=$id, agentId='$agentId', versionNumber='$versionNumber', publishStatus=$publishStatus)"
    }

    /**
     * 获取发布状态枚举
     */
    fun getPublishStatusEnum(): PublishStatus? {
        return publishStatus.let { PublishStatus.fromCode(it) } // publishStatus 非空，可直接使用
    }

    /**
     * 更新发布状态
     */
    fun updatePublishStatus(status: PublishStatus) {
        this.publishStatus = status.code
        this.reviewTime = LocalDateTime.now()
        // updatedAt 将由 BaseEntity 的 @PreUpdate 自动更新
    }

    /**
     * 拒绝发布
     */
    fun reject(reason: String) {
        this.publishStatus = PublishStatus.REJECTED.code
        this.rejectReason = reason
        this.reviewTime = LocalDateTime.now()
        // updatedAt 将由 BaseEntity 的 @PreUpdate 自动更新
    }

    /**
     * 从Agent实体创建一个新的版本实体
     */
    companion object {
        @JvmStatic
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
                changeLog = changeLog,
                agentType = agent.agentType,
                userId = agent.userId,
                publishedAt = now,
                publishStatus = PublishStatus.REVIEWING.code,
                reviewTime = now
            )
        }
    }
}