package cn.cotenite.infrastructure.verification

import cn.hutool.captcha.CaptchaUtil
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 图形验证码工具类
 */
object CaptchaUtils {

    private const val EXPIRATION_MINUTES = 5L

    private data class CaptchaInfo(
        val code: String,
        val expirationTime: Long
    )

    data class CaptchaResult(
        val uuid: String,
        val imageBase64: String
    )

    private val captchaMap = ConcurrentHashMap<String, CaptchaInfo>()

    fun generateCaptcha(): CaptchaResult {
        val captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 100)
        val uuid = UUID.randomUUID().toString()
        captchaMap[uuid] = CaptchaInfo(
            code = captcha.code,
            expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(EXPIRATION_MINUTES)
        )
        return CaptchaResult(uuid, captcha.imageBase64Data)
    }

    fun verifyCaptcha(uuid: String?, code: String?): Boolean {
        if (uuid.isNullOrBlank() || code.isNullOrBlank()) return false
        val captchaInfo = captchaMap[uuid] ?: return false
        if (System.currentTimeMillis() > captchaInfo.expirationTime) {
            captchaMap.remove(uuid)
            return false
        }
        return captchaInfo.code.equals(code, ignoreCase = true).also { matched ->
            if (matched) captchaMap.remove(uuid)
        }
    }

    fun cleanExpiredCaptchas() {
        val currentTime = System.currentTimeMillis()
        captchaMap.entries.removeIf { (_, value) -> value.expirationTime < currentTime }
    }
}
