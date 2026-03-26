package cn.cotenite.infrastructure.utils

/**
 * 参数校验工具
 */
object ValidationUtils {

    fun requireNotBlank(value: String?, field: String) =
        require(!value.isNullOrBlank()) { "$field 不能为空" }

    fun requireTrue(condition: Boolean, message: String) =
        require(condition) { message }
}
