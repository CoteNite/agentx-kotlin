package cn.cotenite.interfaces.dto.tool.request

import cn.cotenite.infrastructure.exception.ParamValidationException
import jakarta.validation.constraints.NotBlank
import java.util.regex.Pattern

/**
 * @author  yhk
 * Description  
 * Date  2026/4/8 17:32
 */
data class MarketToolRequest (
    @field:NotBlank(message = "工具 id 不可为空")
    var toolId: String? = null,

    @field:NotBlank(message = "版本号不能为空")
    var version: String? = null,

    var changeLog: String? = null
) {

    companion object {
        private val VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$")
    }

    /**
     * 验证版本号格式
     */
    fun validate() {
        val v = version ?: throw ParamValidationException("versionNumber", "版本号不能为空")
        if (!VERSION_PATTERN.matcher(v).matches()) {
            throw ParamValidationException("versionNumber", "版本号必须遵循 x.y.z 格式")
        }
    }

    /**
     * 比较版本号是否大于给定的版本号
     */
    fun isVersionGreaterThan(lastVersion: String?): Boolean {
        val currentVersion = version ?: return false

        if (lastVersion.isNullOrBlank()) {
            return true // 如果没有上一个版本，当前版本肯定更大
        }

        // 验证格式
        if (!VERSION_PATTERN.matcher(currentVersion).matches() || !VERSION_PATTERN.matcher(lastVersion).matches()) {
            throw ParamValidationException("版本号", "版本号必须遵循 x.y.z 格式")
        }

        // 核心逻辑简化：将 x.y.z 转换为 List<Int>
        val currentParts = currentVersion.split(".").map { it.toInt() }
        val lastParts = lastVersion.split(".").map { it.toInt() }

        // 利用 Kotlin 集合的比较逻辑
        // List 在比较时会按顺序对比元素，这正好符合版本号比较规则
        for (i in 0 until 3) {
            if (currentParts[i] > lastParts[i]) return true
            if (currentParts[i] < lastParts[i]) return false
        }

        return false
    }
}