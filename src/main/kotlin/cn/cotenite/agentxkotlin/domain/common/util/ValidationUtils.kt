package cn.cotenite.agentxkotlin.domain.common.util

import cn.cotenite.agentxkotlin.domain.common.exception.ParamValidationException
import java.util.regex.Pattern

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 11:29
 */
object ValidationUtils {

    private val VERSION_PATTERN  = Pattern.compile("^\\d+\\.\\d+\\.\\d+$")

    /**
     * 校验参数不为空
     */
    fun notNull(value: Any?, paramName: String) {
        if (value == null) {
            throw ParamValidationException(paramName, "不能为空")
        }
    }

    /**
     * 校验字符串不为空
     */
    fun notEmpty(value: String?, paramName: String) {
        if (value.isNullOrBlank()) {
            throw ParamValidationException(paramName, "不能为空")
        }
    }

    /**
     * 校验集合不为空
     */
    fun notEmpty(collection: Collection<*>?, paramName: String) {
        if (collection.isNullOrEmpty()) {
            throw ParamValidationException(paramName, "不能为空")
        }
    }

    /**
     * 校验字符串长度
     */
    fun length(value: String?, min: Int, max: Int, paramName: String) {
        notEmpty(value, paramName)

        val length = value!!.length
        if (length < min || length > max) {
            throw ParamValidationException(paramName, "必须在${min}-${max}之间，当前值: $value")
        }
    }

    /**
     * 校验数值范围
     */
    fun range(value: Int, min: Int, max: Int, paramName: String) {
        if (value < min || value > max) {
            throw ParamValidationException(paramName, "必须在${min}-${max}之间，当前值: $value")
        }
    }

    /**
     * 校验版本号格式是否正确
     */
    fun validVersionFormat(version: String?, paramName: String) {
        notEmpty(version, paramName)
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw ParamValidationException(
                paramName,
                "版本号格式不正确，应为 X.Y.Z 格式，例如 1.0.0"
            )
        }
    }


}
