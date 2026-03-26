package cn.cotenite.domain.llm.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.infrastructure.converter.ProviderConfigConverter
import cn.cotenite.infrastructure.converter.ProviderProtocolConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * 服务商实体
 */
@TableName("providers")
class ProviderEntity : BaseEntity() {
    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null
    var userId: String? = null

    @TableField(typeHandler = ProviderProtocolConverter::class)
    var protocol: ProviderProtocol? = null
    var name: String? = null
    var description: String? = null

    @TableField(typeHandler = ProviderConfigConverter::class)
    var config: ProviderConfig? = null
    var isOfficial: Boolean = false
    var status: Boolean = true

    fun isActive() {
        if (!status) throw BusinessException("服务商未激活")
    }
}
