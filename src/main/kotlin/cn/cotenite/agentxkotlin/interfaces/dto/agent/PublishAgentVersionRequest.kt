package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.infrastructure.exception.ParamValidationException
import jakarta.validation.constraints.NotBlank
import java.util.regex.Pattern

/**
 * 发布Agent版本请求
 */
data class PublishAgentVersionRequest(
    @field:NotBlank(message = "版本号不能为空")
    var versionNumber: String,
    var changeLog: String
) {
    companion object {
        // 版本号正则表达式，验证x.y.z格式
        private val VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+$")
    }

    /**
     * 校验请求参数
     */
    fun validate() {
        // 验证版本号格式
        if (versionNumber?.let { !VERSION_PATTERN.matcher(it).matches() } == true) {
            throw ParamValidationException("versionNumber", "版本号必须遵循 x.y.z 格式")
        }

        if (changeLog.isNullOrBlank()) {
            throw ParamValidationException("changeLog", "变更日志不能为空")
        }
    }

    /**
     * 比较版本号是否大于给定的版本号
     * 
     * @param lastVersion 上一个版本号
     * @return 如果当前版本号大于lastVersion则返回true，否则返回false
     */
    fun isVersionGreaterThan(lastVersion: String?): Boolean {
        if (lastVersion.isNullOrBlank()) {
            return true // 如果没有上一个版本，当前版本肯定更大
        }

        val currentVersion = versionNumber ?: return false

        // 确保两个版本号都符合格式
        if (!VERSION_PATTERN.matcher(currentVersion).matches() ||
            !VERSION_PATTERN.matcher(lastVersion).matches()) {
            throw ParamValidationException("versionNumber", "版本号必须遵循 x.y.z 格式")
        }

        // 分割版本号
        val current = currentVersion.split(".")
        val last = lastVersion.split(".")

        val currentMajor = current[0].toInt()
        val lastMajor = last[0].toInt()
        if (currentMajor > lastMajor) return true
        if (currentMajor < lastMajor) return false

        // 主版本号相同，比较次版本号
        val currentMinor = current[1].toInt()
        val lastMinor = last[1].toInt()
        if (currentMinor > lastMinor) return true
        if (currentMinor < lastMinor) return false

        // 主版本号和次版本号都相同，比较修订版本号
        val currentPatch = current[2].toInt()
        val lastPatch = last[2].toInt()

        return currentPatch > lastPatch
    }
}