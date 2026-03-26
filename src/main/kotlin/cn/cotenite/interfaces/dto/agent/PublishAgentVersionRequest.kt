package cn.cotenite.interfaces.dto.agent

import jakarta.validation.constraints.NotBlank
import cn.cotenite.infrastructure.exception.ParamValidationException

/**
 * 发布Agent版本请求
 */
data class PublishAgentVersionRequest(
    @field:NotBlank(message = "版本号不能为空")
    var versionNumber: String? = null,
    var changeLog: String? = null
) {

    companion object {
        private val VERSION_PATTERN = Regex("^\\d+\\.\\d+\\.\\d+$")
    }

    /**
     * 校验请求参数
     */
    fun validate() {
        val currentVersion = versionNumber
        if (currentVersion.isNullOrBlank() || !VERSION_PATTERN.matches(currentVersion)) {
            throw ParamValidationException("versionNumber", "版本号必须遵循 x.y.z 格式")
        }
        if (changeLog.isNullOrBlank()) {
            throw ParamValidationException("changeLog", "变更日志不能为空")
        }
    }

    /**
     * 比较版本号是否大于给定版本号
     */
    fun isVersionGreaterThan(lastVersion: String?): Boolean {
        if (lastVersion.isNullOrBlank()) return true

        val currentVersion = versionNumber
        if (
            currentVersion.isNullOrBlank() ||
            !VERSION_PATTERN.matches(currentVersion) ||
            !VERSION_PATTERN.matches(lastVersion)
        ) {
            throw ParamValidationException("versionNumber", "版本号必须遵循 x.y.z 格式")
        }

        val (currentMajor, currentMinor, currentPatch) = currentVersion.split('.').map(String::toInt)
        val (lastMajor, lastMinor, lastPatch) = lastVersion.split('.').map(String::toInt)

        return when {
            currentMajor != lastMajor -> currentMajor > lastMajor
            currentMinor != lastMinor -> currentMinor > lastMinor
            else -> currentPatch > lastPatch
        }
    }
}
