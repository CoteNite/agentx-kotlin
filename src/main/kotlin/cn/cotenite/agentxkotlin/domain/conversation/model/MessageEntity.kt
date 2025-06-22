package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.domain.conversation.constant.Role
import cn.cotenite.agentxkotlin.infrastructure.converter.RoleConverter
import cn.cotenite.agentxkotlin.infrastructure.entity.BaseEntity
import jakarta.persistence.*

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:25
 */
@Entity // 标记这是一个JPA实体
@Table(name = "messages") // 映射到数据库表名
open class MessageEntity(
    /**
     * 消息唯一ID
     */
    @field:Id // 标记为主键
    @field:GeneratedValue(strategy = GenerationType.UUID) // 使用UUID作为ID生成策略
    @field:Column(name = "id", nullable = false, updatable = false) // 明确列名和属性
    var id: String = "",

    /**
     * 所属会话ID
     */
    @field:Column(name = "session_id", nullable = false)
    var sessionId: String ="",

    /**
     * 消息角色 (user, assistant, system)
     * 使用 @Convert 和 RoleConverter 处理枚举到Integer的映射
     */
    @field:Column(name = "role", nullable = false)
    @Convert(converter = RoleConverter::class)
    var role: Role, // Role 类型通常不可为 null，但为了初始化可以暂时设为可空

    /**
     * 消息内容
     */
    @field:Column(name = "content", columnDefinition = "TEXT") // 内容可能很长，使用 TEXT 类型
    var content: String? = null,

    /**
     * Token数量
     */
    @field:Column(name = "token_count", nullable = false)
    var tokenCount: Int = 0, // 提供默认值，通常不为空

    /**
     * 服务提供商
     */
    @field:Column(name = "provider")
    var provider: String? = null,

    /**
     * 使用的模型
     */
    @field:Column(name = "model")
    var model: String? = null,

    /**
     * 消息元数据 (假设存储为JSON字符串)
     */
    @field:Column(name = "metadata", columnDefinition = "json") // 假设存储为JSON
    var metadata: String? = null // 如果是复杂对象，可以考虑再写一个Converter

) : BaseEntity() { // 继承BaseEntity

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as MessageEntity
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "MessageEntity(id=$id, sessionId='$sessionId', role=$role, tokenCount=$tokenCount)"
    }
}