package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.infrastructure.converter.StringListConverter
import cn.cotenite.agentxkotlin.infrastructure.entity.BaseEntity
import jakarta.persistence.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:11
 */
@Entity // 标记这是一个JPA实体
@Table(name = "context")
open class ContextEntity(
    /**
     * 上下文唯一ID
     */
    @field:Id // 标记为主键
    @field:GeneratedValue(strategy = GenerationType.UUID) // 使用UUID作为ID生成策略
    @field:Column(name = "id", nullable = false, updatable = false) // 明确列名和属性
    var id: String? = null,

    /**
     * 所属会话ID
     */
    @field:Column(name = "session_id", nullable = false)
    var sessionId: String? = null,

    /**
     * 活跃消息ID列表，JSON数组字符串
     * 使用自定义的 StringListConverter 将其映射为 List<String>
     */
    @field:Column(name = "active_messages", columnDefinition = "json") // 数据库列类型定义为JSON
    @Convert(converter = StringListConverter::class) // 使用StringListConverter
    var activeMessages: MutableList<String> = mutableListOf(), // 直接存储为List<String>

    /**
     * 历史消息摘要
     */
    @field:Column(name = "summary", length = 4096) // 摘要通常较长，设置一个合理长度
    var summary: String? = null

) : BaseEntity() { // 继承BaseEntity，BaseEntity应使用 @MappedSuperclass

    // JPA要求实体类有无参构造函数。
    // 如果你使用了 'kotlin-noarg' Gradle 插件并正确配置，通常不需要手动编写。

    // equals() 和 hashCode() 的实现对于JPA实体至关重要
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as ContextEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "ContextEntity(id=$id, sessionId='$sessionId', activeMessagesCount=${activeMessages.size})"
    }

    /**
     * 创建新的上下文
     */
    companion object {
        @JvmStatic
        fun createNew(sessionId: String): ContextEntity {
            return ContextEntity(
                sessionId = sessionId,
                activeMessages = mutableListOf(), // 初始化为空列表
                summary = null // 默认无摘要
                // createdAt 和 updatedAt 由 BaseEntity 的 @PrePersist 自动设置
            )
        }
    }

    /**
     * 添加消息到活跃消息列表
     */
    fun addMessage(messageId: String) {
        this.activeMessages.add(messageId)
        // updatedAt 将由 BaseEntity 的 @PreUpdate 自动更新
    }

    /**
     * 获取活跃消息ID列表 (直接返回属性，因为属性已经是 List<String>)
     */
    fun getActiveMessageIds(): List<String> {
        return this.activeMessages
    }

    /**
     * 设置活跃消息列表
     */
    fun setActiveMessageIds(messageIds: List<String>) {
        this.activeMessages = messageIds.toMutableList() // 复制一份可变列表
        // updatedAt 将由 BaseEntity 的 @PreUpdate 自动更新
    }

    /**
     * 清空上下文
     */
    fun clear() {
        this.activeMessages.clear() // 清空列表
        this.summary = null
        // updatedAt 将由 BaseEntity 的 @PreUpdate 自动更新
    }
}