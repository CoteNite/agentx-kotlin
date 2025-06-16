package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.common.exception.ParamValidationException
import java.util.regex.Pattern


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 14:00
 */
data class PublishAgentVersionRequest(
    val versionNumber: String,
    val changeLog: String
){
    companion object{
        private val VERSION_PATTERN= Pattern.compile("^\\d+\\.\\d+\\.\\d+$")
    }

    fun validate() {
        if (versionNumber.trim { it <= ' ' }.isEmpty()) {
            throw ParamValidationException("versionNumber", "版本号不能为空")
        }

        if (!VERSION_PATTERN.matcher(versionNumber).matches()) {
            throw ParamValidationException("versionNumber", "版本号必须遵循 x.y.z 格式")
        }

        if (changeLog.trim { it <= ' ' }.isEmpty()) {
            throw ParamValidationException("changeLog", "变更日志不能为空")
        }
    }


    /**
     * 比较版本号是否大于给定的版本号
     *
     * @param lastVersion 上一个版本号
     * @return 如果当前版本号大于lastVersion则返回true，否则返回false
     */
    fun isVersionGreaterThan(lastVersion: String): Boolean {
        if (lastVersion.trim { it <= ' ' }.isEmpty()) {
            return true // 如果没有上一个版本，当前版本肯定更大
        }

        // 确保两个版本号都符合格式
        if (!VERSION_PATTERN.matcher(versionNumber).matches() ||
            !VERSION_PATTERN.matcher(lastVersion).matches()
        ) {
            throw ParamValidationException("versionNumber", "版本号必须遵循 x.y.z 格式")
        }

        // 分割版本号
        val current = versionNumber.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val last = lastVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // 比较主版本号
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
