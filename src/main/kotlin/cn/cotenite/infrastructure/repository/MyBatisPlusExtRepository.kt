package cn.cotenite.infrastructure.repository

import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * MyBatis扩展仓储接口
 */
interface MyBatisPlusExtRepository<T> : BaseMapper<T> {

    fun checkedUpdate(entity: T, updateWrapper: Wrapper<T>) =
        ensureAffected(update(entity, updateWrapper))

    fun checkedUpdate(updateWrapper: Wrapper<T>) =
        ensureAffected(update(null, updateWrapper))

    fun checkedUpdateById(entity: T) =
        ensureAffected(updateById(entity))

    fun checkedDelete(deleteWrapper: Wrapper<T>) =
        ensureAffected(delete(deleteWrapper))

    fun checkInsert(entity: T) =
        ensureAffected(insert(entity))

    private fun ensureAffected(affected: Int) {
        if (affected == 0) throw BusinessException("数据更新失败")
    }
}
