package cn.cotenite.agentxkotlin.domain.agent.model

import cn.cotenite.agentxkotlin.infrastructure.typehandler.JsonTypeHandler
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model
import org.apache.ibatis.type.JdbcType
import java.time.LocalDateTime

/**
 * @Author RichardYoung
 * @Description Agent版本实体类，代表一个Agent的发布版本
 * @Date 2025/6/16 11:44
 *
 * Kotlin 的数据类自动生成 equals(), hashCode(), toString(), copy() 方法，
 * 并且为主构造函数中的属性自动生成 getter/setter (对于 var 属性) 或 getter (对于 val 属性)。
 */
@TableName(value = "agent_versions", autoResultMap = true)
class AgentVersionEntity(
    /**
     * 版本唯一ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String?=null, // 数据库中为 NOT NULL，所以这里是非可空类型

    /**
     * 关联的Agent ID
     */
    @TableField("agent_id")
    var agentId: String, // 数据库中为 NOT NULL，所以这里是非可空类型

    /**
     * Agent名称
     */
    @TableField("name")
    var name: String, // 数据库中为 NOT NULL，所以这里是非可空类型

    /**
     * Agent头像URL
     */
    @TableField("avatar")
    var avatar: String? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null

    /**
     * Agent描述
     */
    @TableField("description")
    var description: String? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null

    /**
     * 版本号，如1.0.0
     */
    @TableField("version_number")
    var versionNumber: String, // 数据库中为 NOT NULL，所以这里是非可空类型

    /**
     * Agent系统提示词
     */
    @TableField("system_prompt")
    var systemPrompt: String? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null

    /**
     * 欢迎消息
     */
    @TableField("welcome_message")
    var welcomeMessage: String? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null

    /**
     * 模型配置，包含模型类型、温度等参数
     * 默认值设置为 ModelConfig()，与 Java 无参构造函数逻辑一致
     */
    @TableField(value = "model_config", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    var modelConfig: ModelConfig? = ModelConfig(), // 数据库中为 NULLABLE，但此处您想给默认值，如果确保不为 null 可保持非可空

    /**
     * Agent可使用的工具列表
     * 默认值设置为可变空列表
     */
    @TableField(value = "tools", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    var tools: MutableList<AgentTool>? = mutableListOf(), // 数据库中为 NULLABLE，但此处您想给默认值，如果确保不为 null 可保持非可空

    /**
     * 关联的知识库ID列表
     */
    @TableField(value = "knowledge_base_ids", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    var knowledgeBaseIds: MutableList<String>? = mutableListOf(), // 数据库中为 NULLABLE，但此处您想给默认值，如果确保不为 null 可保持非可空

    /**
     * 版本更新日志
     */
    @TableField("change_log")
    var changeLog: String? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null

    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     */
    @TableField("agent_type")
    var agentType: Int? = null, // 数据库中为 NULLABLE，并有默认值 1，但 Kotlin 仍需标记为可空类型，并默认 null

    /**
     * 发布状态：1-审核中, 2-已发布, 3-拒绝, 4-已下架
     */
    @TableField("publish_status")
    var publishStatus: Int? = null, // 数据库中为 NULLABLE，并有默认值 1，但 Kotlin 仍需标记为可空类型，并默认 null

    /**
     * 审核拒绝原因
     */
    @TableField("reject_reason")
    var rejectReason: String? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null

    /**
     * 审核时间
     */
    @TableField("review_time")
    var reviewTime: LocalDateTime? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null

    /**
     * 发布时间
     */
    @TableField("published_at")
    var publishedAt: LocalDateTime? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null

    /**
     * 创建者用户ID
     */
    @TableField("user_id")
    var userId: String, // 数据库中为 NOT NULL，所以这里是非可空类型

    /**
     * 工具预设参数
     */
    @TableField(value = "tool_preset_params", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    var toolPresetParams: String? = null, // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null
    // 注意：如果 tool_preset_params 也是一个 JSON 对象，您可能需要定义一个像 ModelConfig 类似的自定义类，并在这里使用该类型。
    // 为了与您提供的 Java DTO/Entity 保持一致，这里暂时使用 String?。

    /**
     * 多模态能力
     */
    @TableField("multi_modal")
    var multiModal: Boolean? = null, // 数据库中为 NULLABLE，并有默认值 false，但 Kotlin 仍需标记为可空类型，并默认 null

    /**
     * 创建时间
     */
    @TableField("created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(), // 数据库中为 NOT NULL，并有 CURRENT_TIMESTAMP 默认值

    /**
     * 最后更新时间
     */
    @TableField("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(), // 数据库中为 NOT NULL，并有 CURRENT_TIMESTAMP 默认值

    /**
     * 删除时间（软删除）
     */
    @TableField("deleted_at")
    var deletedAt: LocalDateTime? = null // 数据库中为 NULLABLE，所以这里是可空类型，并默认 null
) : Model<AgentVersionEntity>() {



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
                agentType = agent.agentType,
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
