package cn.cotenite.agentxkotlin.domain.agent.model

import cn.cotenite.agentxkotlin.infrastructure.typehandler.JsonTypeHandler
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import org.apache.ibatis.type.JdbcType
import java.time.LocalDateTime

/**
 * @Author RichardYoung
 * @Description Agent实体类
 * @Date 2025/6/16 03:01
 */
@TableName(value = "agents", autoResultMap = true)
data class AgentEntity(
    /**
     * Agent 唯一ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    val id: String, // 数据库: NOT NULL (true -> false)

    /**
     * Agent 名称
     */
    @TableField("name")
    val name: String, // 数据库: NOT NULL (true -> false)

    /**
     * Agent 头像URL
     */
    @TableField("avatar")
    val avatar: String? = null, // 数据库: NULLABLE (false -> true)

    /**
     * Agent 描述
     */
    @TableField("description")
    val description: String? = null, // 数据库: NULLABLE (false -> true)

    /**
     * Agent 系统提示词
     */
    @TableField("system_prompt")
    val systemPrompt: String? = null, // 数据库: NULLABLE (false -> true)

    /**
     * 欢迎消息
     */
    @TableField("welcome_message")
    val welcomeMessage: String? = null, // 数据库: NULLABLE (false -> true)

    /**
     * 模型配置，包含模型类型、温度等参数
     * 注意：数据库中 'tool_ids' 对应 JSONB，这里可能是 'mode_config' 的误写或合并
     * 假设 ModelConfig 是可空但通常有默认值
     */
    @TableField(value = "model_config", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    val modelConfig: ModelConfig? = null,

    /**
     * Agent 可使用的工具列表
     * 数据库字段名为 'tool_ids'，类型是 jsonb，且可空。
     */
    @TableField(value = "tools", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    val tools: MutableList<AgentTool>? = null,

    /**
     * 关联的知识库ID列表
     * 数据库字段名与 `AgentVersionEntity` 相同，是 `knowledge_base_ids` 且可空
     */
    @TableField(value = "knowledge_base_ids", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    val knowledgeBaseIds: MutableList<String>? = null,

    /**
     * 已发布的版本号ID
     * 数据库字段名为 'published_version'
     */
    @TableField("published_version")
    var publishedVersion: String? = null,

    /**
     * 是否启用
     */
    @TableField("enabled")
    var enabled: Boolean = true,

    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     */
    @TableField("agent_type")
    val agentType: Int = 1,

    /**
     * 创建者用户ID
     */
    @TableField("user_id")
    val userId: String, // 数据库: NOT NULL (true -> false)

    /**
     * 工具预设参数
     */
    @TableField(value = "tool_preset_params", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    val toolPresetParams: String? = null, // 数据库: NULLABLE (false -> true)

    /**
     * 多模态能力
     */
    @TableField("multi_modal")
    val multiModal: Boolean = false, // 数据库: NULLABLE (false -> true), 默认值为 false。可空但此处给默认值

    /**
     * 创建时间
     */
    @TableField("create_at") // 注意：您的表数据是 created_at，这里是 create_at
    val createdAt: LocalDateTime = LocalDateTime.now(), // 数据库: NOT NULL (true -> false), 默认值 CURRENT_TIMESTAMP

    /**
     * 最后更新时间
     */
    @TableField("update_at") // 注意：您的表数据是 updated_at，这里是 update_at
    var updatedAt: LocalDateTime = LocalDateTime.now(), // 数据库: NOT NULL (true -> false), 默认值 CURRENT_TIMESTAMP

    /**
     * 删除时间（软删除）
     */
    @TableField("deleted_at")
    var deletedAt: LocalDateTime? = null // 数据库: NULLABLE (false -> true)
){
    fun toDTO(): AgentDTO {
        return AgentDTO(
            id = this.id,
            name = this.name,
            avatar = this.avatar,
            description = this.description,
            systemPrompt = this.systemPrompt,
            welcomeMessage = this.welcomeMessage,
            modeConfig = this.modelConfig,
            tools = this.tools,
            knowledgeBaseIds = this.knowledgeBaseIds,
            publishedVersion = this.publishedVersion,
            enabled = this.enabled,
            agentType =this.agentType,
            userId = this.userId,
            createAt = this.createdAt,
            updateAt = this.updatedAt
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
