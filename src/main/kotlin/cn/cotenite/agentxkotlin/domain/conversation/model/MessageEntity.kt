package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.domain.conversation.dto.MessageDTO
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.*

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:25
 */
@Entity
@Table(name = "messages")
open class MessageEntity(
    /**
     * 消息唯一ID
     * 数据库: NOT NULL
     */
    @Id
    @Column(name = "id")
    val id: String = UUID.randomUUID().toString(),

    /**
     * 所属会话ID
     * 数据库: NOT NULL
     */
    @Column(name = "session_id", nullable = false)
    val sessionId: String,

    /**
     * 消息角色 (user, assistant, system)
     * 数据库: NOT NULL
     */
    @Column(name = "role", nullable = false)
    val role: String,

    /**
     * 消息内容
     * 数据库: NOT NULL
     */
    @Column(name = "content", nullable = false)
    val content: String,

    /**
     * 消息类型 (例如: 'TEXT', 'IMAGE', 'TOOL_CALL')
     * 数据库: NOT NULL, 默认值 'TEXT'
     */
    @Column(name = "message_type", nullable = false)
    val messageType: String = "TEXT",

    /**
     * Token数量
     * 数据库: NOT NULL, 默认值 0
     */
    @Column(name = "token_count", nullable = false)
    val tokenCount: Int = 0,

    /**
     * 服务提供商
     * 数据库: NOT NULL
     */
    @Column(name = "provider", nullable = false)
    val provider: String,

    /**
     * 使用的模型
     * 数据库: NOT NULL
     */
    @Column(name = "model", nullable = false)
    val model: String,

    /**
     * 消息元数据 (JSONB 类型)
     * 数据库: NULLABLE
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    val metadata: String? = null,

    /**
     * 消息中包含的文件URL列表 (JSONB 类型)
     * 数据库: NULLABLE
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "file_urls", columnDefinition = "jsonb")
    val fileUrls: String? = null,

    /**
     * 创建时间
     * 数据库: NOT NULL, 默认 CURRENT_TIMESTAMP
     */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 更新时间
     * 数据库: NOT NULL, 默认 CURRENT_TIMESTAMP
     */
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 删除时间（软删除）
     * 数据库: NULLABLE
     */
    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null
) {


    /**
     * 伴生对象，用于存放静态工厂方法
     */
    companion object {
        /**
         * 创建用户消息
         */
        fun createUserMessage(sessionId: String, content: String): MessageEntity {
            val now = LocalDateTime.now()
            return MessageEntity(
                sessionId = sessionId,
                role = "user",
                content = content,
                createdAt = now,
                provider = "",
                model = ""
            )
        }

        /**
         * 创建系统消息
         */
        fun createSystemMessage(sessionId: String, content: String): MessageEntity {
            val now = LocalDateTime.now()
            return MessageEntity(
                sessionId = sessionId,
                role = "system",
                content = content,
                createdAt = now,
                provider = "",
                model = ""
            )
        }

        /**
         * 创建助手消息
         */
        fun createAssistantMessage(
            sessionId: String,
            content: String,
            provider: String,
            model: String,
            tokenCount: Int? = null // 可空以便不传入时使用默认值0
        ): MessageEntity{
            val now = LocalDateTime.now()
            return MessageEntity(
                sessionId = sessionId,
                role = "assistant",
                content = content,
                createdAt = now,
                provider = provider,
                model = model,
                tokenCount = tokenCount ?: 0
            )
        }
    }


    fun toDTO(): MessageDTO {
        return MessageDTO(
            id = this.id,
            role = this.role,
            content = this.content,
            createdAt = this.createdAt,
            provider = this.provider,
            model = this.model,
        )
    }

}
