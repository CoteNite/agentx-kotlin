package cn.cotenite.interfaces.api.portal.user

import jakarta.servlet.http.HttpServletRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.application.user.service.LoginAppService
import cn.cotenite.infrastructure.verification.CaptchaUtils
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.user.request.GetCaptchaRequest
import cn.cotenite.interfaces.dto.user.request.LoginRequest
import cn.cotenite.interfaces.dto.user.request.RegisterRequest
import cn.cotenite.interfaces.dto.user.request.ResetPasswordRequest
import cn.cotenite.interfaces.dto.user.request.SendEmailCodeRequest
import cn.cotenite.interfaces.dto.user.request.SendResetPasswordCodeRequest
import cn.cotenite.interfaces.dto.user.request.VerifyEmailCodeRequest
import cn.cotenite.interfaces.dto.user.request.VerifyResetPasswordCodeRequest
import cn.cotenite.interfaces.dto.user.response.CaptchaResponse

/**
 * 登录注册控制器
 */
@RestController
@RequestMapping
class LoginController(
    private val loginAppService: LoginAppService
) {

    @PostMapping("/login")
    fun login(@RequestBody @Validated loginRequest: LoginRequest): Result<Map<String, Any>> =
        Result.success("登录成功", mapOf("token" to loginAppService.login(loginRequest)))

    @PostMapping("/register")
    fun register(@RequestBody @Validated registerRequest: RegisterRequest): Result<Void> {
        loginAppService.register(registerRequest)
        return Result.success<Void>().apply { message = "注册成功" }
    }

    @PostMapping("/get-captcha")
    fun getCaptcha(@RequestBody(required = false) request: GetCaptchaRequest?): Result<CaptchaResponse> =
        CaptchaUtils.generateCaptcha().let {
            Result.success(CaptchaResponse(it.uuid, it.imageBase64))
        }

    @PostMapping("/send-email-code")
    fun sendEmailCode(
        @RequestBody @Validated request: SendEmailCodeRequest,
        httpRequest: HttpServletRequest
    ): Result<Void> {
        loginAppService.sendEmailVerificationCode(
            request.email.orEmpty(),
            request.captchaUuid.orEmpty(),
            request.captchaCode.orEmpty(),
            getClientIp(httpRequest)
        )
        return Result.success<Void>().apply { message = "验证码已发送，请查收邮件" }
    }

    @PostMapping("/send-reset-password-code")
    fun sendResetPasswordCode(
        @RequestBody @Validated request: SendResetPasswordCodeRequest,
        httpRequest: HttpServletRequest
    ): Result<Void> {
        loginAppService.sendResetPasswordCode(
            request.email.orEmpty(),
            request.captchaUuid.orEmpty(),
            request.captchaCode.orEmpty(),
            getClientIp(httpRequest)
        )
        return Result.success<Void>().apply { message = "验证码已发送，请查收邮件" }
    }

    @PostMapping("/verify-email-code")
    fun verifyEmailCode(@RequestBody @Validated request: VerifyEmailCodeRequest): Result<Boolean> =
        if (loginAppService.verifyEmailCode(request.email.orEmpty(), request.code.orEmpty())) {
            Result.success(true).apply { message = "验证码验证成功" }
        } else {
            Result.error(403, "验证码无效或已过期")
        }

    @PostMapping("/verify-reset-password-code")
    fun verifyResetPasswordCode(
        @RequestBody @Validated request: VerifyResetPasswordCodeRequest
    ): Result<Boolean> = if (loginAppService.verifyResetPasswordCode(request.email.orEmpty(), request.code.orEmpty())) {
        Result.success(true).apply { message = "验证码验证成功" }
    } else {
        Result.error(403, "验证码无效或已过期")
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody @Validated request: ResetPasswordRequest): Result<Void> {
        loginAppService.resetPassword(
            request.email.orEmpty(),
            request.newPassword.orEmpty(),
            request.code.orEmpty()
        )
        return Result.success<Void>().apply { message = "密码重置成功" }
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val headers = listOf(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        )
        return headers
            .asSequence()
            .mapNotNull(request::getHeader)
            .firstOrNull { it.isNotBlank() && !it.equals("unknown", ignoreCase = true) }
            ?.split(",")
            ?.first()
            ?.trim()
            ?: request.remoteAddr
    }
}
