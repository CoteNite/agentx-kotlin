package cn.cotenite.domain.conversation.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import cn.cotenite.infrastructure.entity.BaseEntity
import java.time.LocalDateTime

/**
 * 会话实体
 */
@TableName("sessions")
class SessionEntity : BaseEntity() {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null
    @TableField("title")
    var title: String? = null
    @TableField("user_id")
    var userId: String? = null
    @TableField("agent_id")
    var agentId: String? = null
    @TableField("description")
    var description: String? = null
    @TableField("is_archived")
    var isArchived: Boolean = false
    @TableField("metadata")
    var metadata: String? = null

    companion object {
        fun createNew(title: String, userId: String): SessionEntity =
            SessionEntity().apply {
                this.title = title
                this.userId = userId
                isArchived = false
            }
    }

    fun update(title: String?, description: String?) {
        this.title = title
        this.description = description
        updatedAt = LocalDateTime.now()
    }

    fun archive() {
        isArchived = true
        updatedAt = LocalDateTime.now()
    }

    fun unarchive() {
        isArchived = false
        updatedAt = LocalDateTime.now()
    }
}
