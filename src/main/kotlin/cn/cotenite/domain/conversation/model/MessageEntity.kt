package cn.cotenite.domain.conversation.model

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import cn.cotenite.domain.conversation.constant.Role
import cn.cotenite.infrastructure.converter.RoleConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import java.time.LocalDateTime

/**
 * 消息实体
 */
@TableName("messages")
class MessageEntity : BaseEntity() {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null
    @TableField("session_id")
    var sessionId: String? = null

    @TableField(value = "role", typeHandler = RoleConverter::class)
    var role: Role? = null
    @TableField("content")
    var content: String? = null
    @TableField("token_count")
    var tokenCount: Int = 0
    @TableField("provider")
    var provider: String? = null
    @TableField("model")
    var model: String? = null
    @TableField("metadata")
    var metadata: String? = null

    companion object {
        fun create(sessionId: String, role: Role, content: String): MessageEntity {
            val message = MessageEntity()
            message.sessionId = sessionId
            message.role = role
            message.content = content
            message.createdAt = LocalDateTime.now()
            return message
        }
    }
}
