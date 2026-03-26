package cn.cotenite.infrastructure.entity

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableLogic
import java.time.LocalDateTime

/**
 * 基础实体
 */
open class BaseEntity {

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    var createdAt: LocalDateTime? = null

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    var updatedAt: LocalDateTime? = null

    /**
     * 删除时间
     */
    @TableLogic
    var deletedAt: LocalDateTime? = null

    /**
     * 操作人（非数据库字段）
     */
    @TableField(exist = false)
    private var operatedBy: Operator = Operator.USER

    /**
     * 设置为管理员操作
     */
    fun setAdmin() {
        operatedBy = Operator.ADMIN
    }

    /**
     * 是否需要校验 userId
     */
    fun needCheckUserId(): Boolean = operatedBy == Operator.USER

    /**
     * 获取当前操作人
     */
    fun getOperatedBy(): Operator = operatedBy

    /**
     * 设置当前操作人
     */
    fun setOperatedBy(operatedBy: Operator) {
        this.operatedBy = operatedBy
    }
}
