package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.infrastructure.typehandler.JsonTypeHandler
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.activerecord.Model
import org.apache.ibatis.type.JdbcType
import java.time.LocalDateTime
import java.util.*

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:25
 */
@TableName("messages")
data class Message(
    /**
     * 消息唯一ID
     * 数据库: NOT NULL
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    val id: String = UUID.randomUUID().toString(), // 数据库中为 NOT NULL，且通常是主键，可自动生成

    /**
     * 所属会话ID
     * 数据库: NOT NULL
     */
    @TableField("session_id")
    val sessionId: String,

    /**
     * 消息角色 (user, assistant, system)
     * 数据库: NOT NULL
     */
    @TableField("role")
    val role: String,

    /**
     * 消息内容
     * 数据库: NOT NULL
     */
    @TableField("content")
    val content: String,

    /**
     * 消息类型 (例如: 'TEXT', 'IMAGE', 'TOOL_CALL')
     * 数据库: NOT NULL, 默认值 'TEXT'
     */
    @TableField("message_type")
    val messageType: String = "TEXT", // 数据库中默认值为 'TEXT'

    /**
     * Token数量
     * 数据库: NOT NULL, 默认值 0
     */
    @TableField("token_count")
    val tokenCount: Int = 0, // 注意：Java中是 Integer，这里直接用 Kotlin 的 Int，并给出默认值 0

    /**
     * 服务提供商
     * 数据库: NOT NULL
     */
    @TableField("provider")
    val provider: String,

    /**
     * 使用的模型
     * 数据库: NOT NULL
     */
    @TableField("model")
    val model: String,

    /**
     * 消息元数据 (JSONB 类型)
     * 数据库: NULLABLE
     * 假设 metadata 是一个 JSON 字符串，您需要 TypeHandler 来处理它。
     */
    @TableField(value = "metadata", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    val metadata: String? = null, // 数据库中为 NULLABLE，默认 null

    /**
     * 消息中包含的文件URL列表 (JSONB 类型)
     * 数据库: NULLABLE
     * 假设 file_urls 是一个 List<String>，您需要 TypeHandler 来处理它。
     */
    @TableField(value = "file_urls", typeHandler = JsonTypeHandler::class, jdbcType = JdbcType.OTHER)
    val fileUrls: MutableList<String>? = null, // 数据库中为 NULLABLE，默认 null

    /**
     * 创建时间
     * 数据库: NOT NULL, 默认 CURRENT_TIMESTAMP
     */
    @TableField("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(), // 数据库中为 NOT NULL

    /**
     * 更新时间
     * 数据库: NOT NULL, 默认 CURRENT_TIMESTAMP
     */
    @TableField("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(), // 数据库中为 NOT NULL

    /**
     * 删除时间（软删除）
     * 数据库: NULLABLE
     */
    @TableField("deleted_at")
    val deletedAt: LocalDateTime? = null // 数据库中为 NULLABLE，默认 null
): Model<Message>(){


    /**
     * 伴生对象，用于存放静态工厂方法
     */
    companion object {
        /**
         * 创建用户消息
         */
        fun createUserMessage(sessionId: String, content: String): Message {
            val now = LocalDateTime.now()
            return Message(
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
        fun createSystemMessage(sessionId: String, content: String): Message {
            val now = LocalDateTime.now()
            return Message(
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
        ): Message{
            val now = LocalDateTime.now()
            return Message(
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
