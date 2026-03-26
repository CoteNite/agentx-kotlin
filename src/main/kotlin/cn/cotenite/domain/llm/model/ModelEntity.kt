package cn.cotenite.domain.llm.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import org.apache.ibatis.type.JdbcType
import cn.cotenite.domain.llm.model.enums.ModelType
import cn.cotenite.infrastructure.converter.ModelTypeConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * 模型实体
 */
@TableName("models")
class ModelEntity : BaseEntity() {
    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null
    var userId: String? = null
    var providerId: String? = null
    var modelId: String? = null
    var name: String? = null
    var description: String? = null
    var isOfficial: Boolean = false

    @TableField(typeHandler = ModelTypeConverter::class, jdbcType = JdbcType.VARCHAR)
    var type: ModelType? = null
    var status: Boolean = true

    fun isActive() {
        if (!status) throw BusinessException("模型未激活")
    }
}
