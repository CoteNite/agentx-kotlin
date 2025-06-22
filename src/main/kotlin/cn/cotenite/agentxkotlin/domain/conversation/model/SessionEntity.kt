package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.infrastructure.entity.BaseEntity
import jakarta.persistence.*

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:20
 */
@Entity // 标记这是一个 JPA 实体
@Table(name = "sessions") // 映射到数据库表名
open class SessionEntity(
    /**
     * 会话唯一ID
     */
    @field:Id // 标记为主键
    @field:GeneratedValue(strategy = GenerationType.UUID) // 使用 UUID 作为 ID 生成策略
    @field:Column(name = "id", nullable = false, updatable = false) // 明确列名和属性，不可为空且不可更新
    var id: String? = null,

    /**
     * 会话标题
     */
    @field:Column(name = "title", nullable = false, length = 255) // 会话标题通常不为空，设置一个合理长度
    var title: String? = null,

    /**
     * 所属用户ID
     */
    @field:Column(name = "user_id", nullable = false) // 用户 ID 通常不为空
    var userId: String? = null,

    /**
     * 关联的 Agent 版本 ID
     */
    @field:Column(name = "agent_id")
    var agentId: String? = null,

    /**
     * 会话描述
     */
    @field:Column(name = "description", length = 512) // 描述可能较长
    var description: String? = null,

    /**
     * 是否归档
     */
    @field:Column(name = "is_archived", nullable = false) // 布尔值通常不为空，并设置默认值
    var isArchived: Boolean = false,

    /**
     * 会话元数据，可存储其他自定义信息
     */
    @field:Column(name = "metadata", columnDefinition = "json") // 如果是 JSON，建议使用 columnDefinition
    var metadata: String? = null

) : BaseEntity() { // 继承 BaseEntity，BaseEntity 应使用 @MappedSuperclass

    // JPA 要求实体类有无参构造函数。
    // 如果你使用了 'kotlin-noarg' Gradle 插件并正确配置，通常不需要手动编写。

    // equals() 和 hashCode() 的实现对于 JPA 实体至关重要
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as SessionEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "SessionEntity(id=$id, title='$title', userId='$userId', isArchived=$isArchived)"
    }

    /**
     * 创建新会话
     */
    companion object { // 伴生对象，用于定义静态方法
        @JvmStatic // 使得这个方法可以在 Java 代码中像静态方法一样直接调用
        fun createNew(title: String, userId: String): SessionEntity {
            return SessionEntity(
                title = title,
                userId = userId,
                isArchived = false // 默认不归档
                // createdAt 和 updatedAt 将由 BaseEntity 的 @PrePersist 自动设置
            )
        }
    }

    /**
     * 更新会话信息
     */
    fun update(title: String?, description: String?) {
        this.title = title
        this.description = description
        // `updatedAt` 将由 `BaseEntity` 的 `@PreUpdate` 自动处理
    }

    /**
     * 归档会话
     */
    fun archive() {
        this.isArchived = true
        // `updatedAt` 将由 `BaseEntity` 的 `@PreUpdate` 自动处理
    }

    /**
     * 恢复已归档会话
     */
    fun unarchive() {
        this.isArchived = false

    }
}