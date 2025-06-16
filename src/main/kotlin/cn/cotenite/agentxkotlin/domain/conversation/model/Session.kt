package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.infrastructure.typehandler.JsonTypeHandler
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model
import java.time.LocalDateTime
import java.util.*

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:20
 */
@TableName("sessions")
data class Session(
    /**
     * 会话唯一ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    val id: String = UUID.randomUUID().toString(), // 数据库中为 NOT NULL，且通常是主键，可自动生成

    /**
     * 会话标题
     * 数据库中为 NOT NULL
     */
    @TableField("title")
    var title: String, // 在Java中您设置为可变，这里也使用 var

    /**
     * 所属用户ID
     * 数据库中为 NOT NULL
     */
    @TableField("user_id")
    val userId: String, // 通常创建后不变，使用 val

    /**
     * 关联的 Agent ID
     * 数据库中为 NULLABLE
     */
    @TableField("agent_id")
    var agentId: String? = null, // 数据库中为 NULLABLE，默认 null

    /**
     * 会话描述
     * 数据库中为 NULLABLE
     */
    @TableField("description")
    var description: String? = null, // 数据库中为 NULLABLE，默认 null

    /**
     * 是否归档
     * 数据库中为 NULLABLE，默认 false
     */
    @TableField("is_archived")
    var isArchived: Boolean = false, // 数据库中为 NULLABLE，但通常有默认值 false，这里也设置为默认值

    /**
     * 会话元数据，可存储其他自定义信息 (JSONB 类型)
     * 数据库中为 NULLABLE
     * 这里假设 metadata 是一个 JSON 字符串，如果它对应某个具体的对象，可以定义相应的数据类并使用 JsonTypeHandler。
     */
    @TableField(value = "metadata", typeHandler = JsonTypeHandler::class)
    var metadata: String? = null, // 数据库中为 NULLABLE，假设为 String 类型，可根据实际 JSON 结构调整

    /**
     * 创建时间
     * 数据库为 NOT NULL，默认 CURRENT_TIMESTAMP
     */
    @TableField("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(), // 数据库中为 NOT NULL

    /**
     * 最后更新时间
     * 数据库为 NOT NULL，默认 CURRENT_TIMESTAMP
     */
    @TableField("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(), // 数据库中为 NOT NULL，可变以便更新

    /**
     * 删除时间（软删除）
     * 数据库中为 NULLABLE
     */
    @TableField("deleted_at")
    var deletedAt: LocalDateTime? = null // 数据库中为 NULLABLE，默认 null

):Model<Session>(){

    /**
     * 创建新会话的工厂方法
     * Kotlin 推荐使用伴生对象 (companion object) 来存放静态工厂方法。
     */
    companion object {
        fun createNew(title: String, userId: String): Session{
            val now = LocalDateTime.now()
            return Session(
                title = title,
                userId = userId,
                createdAt = now,
                updatedAt = now,
                isArchived = false, // 明确初始状态
                description = null, // 新建时可能没有描述
                agentId = null,     // 新建时可能没有关联 agent
                metadata = null     // 新建时可能没有元数据
            )
        }
    }

    /**
     * 更新会话信息
     * 直接修改属性并更新 updatedAt
     */
    fun update(newTitle: String? = null, newDescription: String? = null, newAgentId: String? = null, newMetadata: String? = null) {
        newTitle?.let { this.title = it }
        newDescription?.let { this.description = it }
        newAgentId?.let { this.agentId = it }
        newMetadata?.let { this.metadata = it }
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 归档会话
     */
    fun archive() {
        this.isArchived = true
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 恢复已归档会话
     */
    fun unArchive() {
        this.isArchived = false
        this.updatedAt = LocalDateTime.now()
    }

    fun toDTO(): SessionDTO {
        return SessionDTO(
            id = this.id,
            title = this.title,
            description = this.description,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            isArchived = this.isArchived
        )
    }
}
