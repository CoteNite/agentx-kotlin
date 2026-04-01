package cn.cotenite.domain.conversation.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import cn.cotenite.infrastructure.converter.ListConverter
import cn.cotenite.infrastructure.entity.BaseEntity

/**
 * 上下文实体
 */
@TableName("context")
class ContextEntity : BaseEntity() {
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null
    @TableField("session_id")
    var sessionId: String? = null

    @TableField(value = "active_messages", typeHandler = ListConverter::class)
    var activeMessages: MutableList<String?> = mutableListOf()
    @TableField("summary")
    var summary: String? = null
}
