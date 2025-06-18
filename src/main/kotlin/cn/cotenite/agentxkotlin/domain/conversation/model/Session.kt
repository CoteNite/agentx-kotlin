package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.domain.conversation.model.converter.StringConverter
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:20
 */
@Entity
@Table(name = "sessions")
data class Session(
    /**
     * 会话唯一ID
     */
    @Id
    @Column(name = "id")
    val id: String = UUID.randomUUID().toString(),

    /**
     * 会话标题
     * 数据库中为 NOT NULL
     */
    @Column(name = "title", nullable = false)
    var title: String,

    /**
     * 所属用户ID
     * 数据库中为 NOT NULL
     */
    @Column(name = "user_id", nullable = false)
    val userId: String,

    /**
     * 关联的 Agent ID
     * 数据库中为 NULLABLE
     */
    @Column(name = "agent_id")
    var agentId: String? = null,

    /**
     * 会话描述
     * 数据库中为 NULLABLE
     */
    @Column(name = "description")
    var description: String? = null,

    /**
     * 是否归档
     * 数据库中为 NULLABLE，默认 false
     */
    @Column(name = "is_archived")
    var isArchived: Boolean = false,

    /**
     * 会话元数据，可存储其他自定义信息 (JSONB 类型)
     * 数据库中为 NULLABLE
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    var metadata: String? = null,

    /**
     * 创建时间
     * 数据库为 NOT NULL，默认 CURRENT_TIMESTAMP
     */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 最后更新时间
     * 数据库为 NOT NULL，默认 CURRENT_TIMESTAMP
     */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 删除时间（软删除）
     * 数据库中为 NULLABLE
     */
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

) {

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
