package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.domain.conversation.model.converter.StringConverter
import com.alibaba.fastjson2.JSON
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:11
 */
@Entity
@Table(name = "context")
data class Context(
    /**
     * 唯一ID
     */
    @Id
    @Column(name = "id")
    val id: String = UUID.randomUUID().toString(),

    /**
     * 关联的会话ID
     */
    @Column(name = "session_id", nullable = false)
    val sessionId: String,

    /**
     * 活跃消息列表 (JSONB 类型)
     * 数据库中为 NULLABLE
     */
    @Column(name = "active_messages", columnDefinition = "jsonb")
    @Convert(converter = StringConverter::class)
    var activeMessages: String? = null,

    /**
     * 会话摘要内容 (TEXT 类型)
     * 数据库中为 NULLABLE
     */
    @Column(name = "summary")
    var summary: String? = null,

    /**
     * 创建时间
     * 数据库为 NOT NULL，默认 CURRENT_TIMESTAMP
     */
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 更新时间
     * 数据库为 NOT NULL，默认 CURRENT_TIMESTAMP
     */
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 删除时间（软删除）
     * 数据库中为 NULLABLE
     */
    @Column(name = "deleted_at")
    val deletedAt: LocalDateTime? = null
) {

    companion object{
        fun createNew(sessionId: String): Context {
            return Context(
                sessionId = sessionId,
                activeMessages = "[]",
                updatedAt = LocalDateTime.now(),
            )
        }
    }

    fun addMessage(messageId:String){
        val messages  = this.parseActiveMessages()
        messages.add(messageId)
        this.activeMessages=this.formatActiveMessages(messages)
        this.updatedAt=LocalDateTime.now()
    }

    fun getActiveMessageIds(): List<String> {
        return parseActiveMessages()
    }

    /**
     * 设置活跃消息列表
     */
    fun setActiveMessageIds(messageIds: List<String>) {
        this.activeMessages = formatActiveMessages(messageIds)
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 清空上下文
     */
    fun clear() {
        this.activeMessages = "[]"
        this.summary = null
        this.updatedAt = LocalDateTime.now()
    }

    private fun parseActiveMessages(): MutableList<String> {
        return JSON.parseArray(activeMessages, String::class.java)?.toMutableList() ?: mutableListOf()
    }

    /**
     * 将 List<String> 格式化为 JSON 字符串。
     * 如果 messages 为 null 或空列表，则返回 "[]"。
     */
    private fun formatActiveMessages(messages: List<String>?): String {
        return if (messages.isNullOrEmpty()) {
            "[]"
        } else {
            JSON.toJSONString(messages)
        }
    }
}
