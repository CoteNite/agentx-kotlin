package cn.cotenite.agentxkotlin.domain.agent.model

import cn.cotenite.agentxkotlin.domain.agent.constant.AgentType
import cn.cotenite.agentxkotlin.infrastructure.converter.AgentModelConfigConverter
import cn.cotenite.agentxkotlin.infrastructure.converter.ListConverter
import cn.cotenite.agentxkotlin.infrastructure.entity.BaseEntity
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
     * Agent唯一ID
     */
    @field:Id // 标记为主键
    @field:GeneratedValue(strategy = GenerationType.UUID) // 使用UUID作为ID生成策略
    // 注意：@TableId(type = IdType.ASSIGN_UUID) 是MyBatis-Plus特有，JPA通常用上面的@GeneratedValue
    @field:Column(name = "id", nullable = false, updatable = false) // 明确列名和属性
    var id: String? = null,

    /**
     * Agent名称
     */
    @field:Column(name = "name") // 映射到数据库列名
    var name: String? = null,

    /**
     * Agent头像URL
     */
    @field:Column(name = "avatar")
    var avatar: String? = null,

    /**
     * Agent描述
     */
    @field:Column(name = "description", length = 512) // 可以设置列的长度
    var description: String? = null,

    /**
     * Agent系统提示词
     */
    @field:Column(name = "system_prompt", length = 2048) // 文本字段通常建议设置长度
    var systemPrompt: String? = null,

    /**
     * 欢迎消息
     */
    @field:Column(name = "welcome_message")
    var welcomeMessage: String? = null,

    /**
     * 模型配置，包含模型类型、温度等参数
     * 使用 @Convert 和 AttributeConverter 处理复杂对象到JSON字符串的映射
     */
    @field:Column(name = "model_config", columnDefinition = "json") // 定义数据库列类型为JSON
    @Convert(converter = AgentModelConfigConverter::class) // 指定JPA转换器
    var modelConfig: AgentModelConfig? = AgentModelConfig.createDefault(),

    /**
     * Agent可使用的工具列表
     * 同样使用 @Convert 处理列表到JSON字符串的映射
     */
    @field:Column(name = "tools", columnDefinition = "json")
    @Convert(converter = ListConverter::class)
    var tools: MutableList<AgentTool>? = mutableListOf(),

    /**
     * 关联的知识库ID列表
     * 同样使用 @Convert 处理列表到JSON字符串的映射
     */
    @field:Column(name = "knowledge_base_ids", columnDefinition = "json")
    @Convert(converter = ListConverter::class)
    var knowledgeBaseIds: MutableList<String>? = mutableListOf(),

    /**
     * 当前发布的版本ID
     */
    @field:Column(name = "published_version")
    var publishedVersion: String? = null,

    /**
     * Agent状态：1-启用，0-禁用
     */
    @field:Column(name = "enabled", nullable = false)
    var enabled: Boolean = true, // Boolean通常不为空，默认启用

    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     */
    @field:Column(name = "agent_type", nullable = false)
    var agentType: Int = AgentType.CHAT_ASSISTANT.code, // Int通常不为空，提供默认值

    /**
     * 创建者用户ID
     */
    @field:Column(name = "user_id")
    var userId: String? = null,

) : BaseEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as AgentEntity
        return id != null && id == other.id // 只有当id不为空时才依赖id进行比较
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0 // 如果id为空，返回0或System.identityHashCode(this)
    }

    override fun toString(): String {
        return "AgentEntity(id=$id, name='$name', agentType=$agentType, enabled=$enabled)"
        // 避免在toString中包含懒加载的关联属性，以防LazyInitializationException
    }

    /**
     * 创建新的Agent对象
     */
    companion object {
        fun createNew(
            name: String,
            description: String,
            avatar: String,
            agentType: Int?,
            userId: String
        ): AgentEntity {
            return AgentEntity(
                name = name,
                description = description,
                avatar = avatar,
                agentType = agentType ?: AgentType.CHAT_ASSISTANT.code, // 使用Elvis运算符处理null
                userId = userId,
                enabled = true // 默认启用
                // createdAt 和 updatedAt 通常由 BaseEntity 的 @PrePersist 自动设置
            )
        }
    }

    /**
     * 更新Agent基本信息
     */
    fun updateBasicInfo(name: String, avatar: String, description: String) {
        this.name = name
        this.avatar = avatar
        this.description = description
        // updatedAt 将由 @PreUpdate 自动更新
    }

    /**
     * 更新Agent配置
     */
    fun updateConfig(
        systemPrompt: String?,
        welcomeMessage: String?,
        modelConfig: AgentModelConfig?,
        tools: MutableList<AgentTool>?,
        knowledgeBaseIds: MutableList<String>?
    ) {
        this.systemPrompt = systemPrompt
        this.welcomeMessage = welcomeMessage
        this.modelConfig = modelConfig
        this.tools = tools
        this.knowledgeBaseIds = knowledgeBaseIds
        // updatedAt 将由 @PreUpdate 自动更新
    }

    /**
     * 启用Agent
     */
    fun enable() {
        this.enabled = true
        // updatedAt 将由 @PreUpdate 自动更新
    }

    /**
     * 禁用Agent
     */
    fun disable() {
        this.enabled = false
        // updatedAt 将由 @PreUpdate 自动更新
    }

    /**
     * 发布新版本
     */
    fun publishVersion(versionId: String) {
        this.publishedVersion = versionId
        // updatedAt 将由 @PreUpdate 自动更新
    }

    /**
     * 软删除
     */
    fun delete() {
        this.deletedAt = LocalDateTime.now() // 手动设置deletedAt
        // updatedAt 将由 @PreUpdate 自动更新
    }

    /**
     * 获取Agent类型枚举
     */
    fun getAgentTypeEnum(): AgentType? {
        return agentType.let { AgentType.fromCode(it) }
    }
}