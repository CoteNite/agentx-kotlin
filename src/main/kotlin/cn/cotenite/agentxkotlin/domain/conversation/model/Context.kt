package cn.cotenite.agentxkotlin.domain.conversation.model

import cn.cotenite.agentxkotlin.infrastructure.typehandler.JsonTypeHandler
import com.alibaba.fastjson2.JSON
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime
import java.util.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:11
 */
@TableName("context")
data class Context(
    /**
     * 唯一ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    val id: String = UUID.randomUUID().toString(),

    /**
     * 关联的会话ID
     */
    @TableField("session_id")
    val sessionId: String, // 数据库为 NOT NULL

    /**
     * 活跃消息列表 (JSONB 类型)
     * 数据库中为 NULLABLE
     * 这里使用 List<Map<String, Any>> 作为示例，表示一个包含JSON对象的列表。
     * 您需要确保有相应的 TypeHandler 来处理 JSONB 到此 Kotlin 类型的转换。
     */
    @TableField("active_messages", typeHandler = JsonTypeHandler::class)
    var activeMessages: String? = null, // 数据库中为 NULLABLE

    /**
     * 会话摘要内容 (TEXT 类型)
     * 数据库中为 NULLABLE
     */
    @TableField("summary")
    var summary: String? = null, // 数据库中为 NULLABLE

    /**
     * 创建时间
     * 数据库为 NOT NULL，默认 CURRENT_TIMESTAMP
     */
    @TableField("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(), // 数据库为 NOT NULL

    /**
     * 更新时间
     * 数据库为 NOT NULL，默认 CURRENT_TIMESTAMP
     */
    @TableField("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(), // 数据库为 NOT NULL

    /**
     * 删除时间（软删除）
     * 数据库中为 NULLABLE
     */
    @TableField("deleted_at")
    val deletedAt: LocalDateTime? = null // 数据库中为 NULLABLE
){

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
