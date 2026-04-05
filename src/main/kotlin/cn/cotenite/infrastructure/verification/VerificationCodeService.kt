package cn.cotenite.infrastructure.verification

import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.verification.storage.CodeStorage
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * 邮箱验证码服务
 */
@Service
class VerificationCodeService(
    private val codeStorage: CodeStorage
) {

    companion object {
        const val BUSINESS_TYPE_REGISTER = "register"
        const val BUSINESS_TYPE_RESET_PASSWORD = "reset_password"
        private const val CODE_LENGTH = 6
        private const val EXPIRATION_MINUTES = 10L
        private const val MAX_DAILY_SEND_COUNT = 10
        private const val MIN_SEND_INTERVAL_SECONDS = 60L
        private const val MAX_DAILY_IP_SEND_COUNT = 20
    }

    private data class LimitInfo(
        var dailyCount: Int = 0,
        var lastSendTime: Long = 0
    )

    private data class IpLimitInfo(
        var dailyCount: Int = 0
    )

    private val limitMap = ConcurrentHashMap<String, LimitInfo>()
    private val ipLimitMap = ConcurrentHashMap<String, IpLimitInfo>()

    fun generateCode(
        email: String,
        captchaUuid: String,
        captchaCode: String,
        ip: String,
        businessType: String
    ): String {
        if (!CaptchaUtils.verifyCaptcha(captchaUuid, captchaCode)) {
            throw BusinessException("图形验证码错误或已过期")
        }

        checkIpLimit(ip)
        checkSendLimit(email)

        val code = (1..CODE_LENGTH)
            .joinToString(separator = "") { Random.nextInt(10).toString() }
        val expirationMillis = TimeUnit.MINUTES.toMillis(EXPIRATION_MINUTES)
        codeStorage.storeCode(storageKey(email, businessType), code, expirationMillis)

        limitMap.compute(email) { _, value ->
            (value ?: LimitInfo()).apply {
                dailyCount += 1
                lastSendTime = System.currentTimeMillis()
            }
        }
        ipLimitMap.compute(ip) { _, value ->
            (value ?: IpLimitInfo()).apply { dailyCount += 1 }
        }

        return code
    }

    fun generateCode(email: String, captchaUuid: String, captchaCode: String, ip: String): String =
        generateCode(email, captchaUuid, captchaCode, ip, BUSINESS_TYPE_REGISTER)

    fun verifyCode(email: String, code: String, businessType: String): Boolean =
        codeStorage.verifyCode(storageKey(email, businessType), code)

    fun verifyCode(email: String, code: String): Boolean =
        verifyCode(email, code, BUSINESS_TYPE_REGISTER)

    fun resetAllCounts() {
        limitMap.clear()
        ipLimitMap.clear()
    }

    private fun storageKey(email: String, businessType: String): String = "$businessType:$email"

    private fun checkSendLimit(email: String) {
        val limitInfo = limitMap[email] ?: return
        val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - limitInfo.lastSendTime)

        if (elapsedSeconds < MIN_SEND_INTERVAL_SECONDS) {
            throw BusinessException("发送过于频繁，请${MIN_SEND_INTERVAL_SECONDS - elapsedSeconds}秒后再试")
        }
        if (limitInfo.dailyCount >= MAX_DAILY_SEND_COUNT) {
            throw BusinessException("今日发送次数已达上限，请明天再试")
        }
    }

    private fun checkIpLimit(ip: String) {
        val limitInfo = ipLimitMap[ip] ?: return
        if (limitInfo.dailyCount >= MAX_DAILY_IP_SEND_COUNT) {
            throw BusinessException("您的IP今日请求次数已达上限，请明天再试")
        }
    }
}
