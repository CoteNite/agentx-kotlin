package cn.cotenite.application.user.service

import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import cn.cotenite.domain.user.model.UserEntity
import cn.cotenite.domain.user.service.UserDomainService
import cn.cotenite.infrastructure.email.EmailService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.utils.JwtUtils
import cn.cotenite.infrastructure.verification.VerificationCodeService
import cn.cotenite.interfaces.dto.user.request.LoginRequest
import cn.cotenite.interfaces.dto.user.request.RegisterRequest

/**
 * 登录应用服务
 */
@Service
class LoginAppService(
    private val userDomainService: UserDomainService,
    private val emailService: EmailService,
    private val verificationCodeService: VerificationCodeService
) {

    fun login(loginRequest: LoginRequest): String =
        userDomainService.login(loginRequest.account.orEmpty(), loginRequest.password.orEmpty())
            .let { JwtUtils.generateToken(it.id ?: throw BusinessException("用户不存在")) }

    fun register(registerRequest: RegisterRequest) {
        val email = registerRequest.email
        val phone = registerRequest.phone
        val password = registerRequest.password.orEmpty()

        if (StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            val code = registerRequest.code
            if (!StringUtils.hasText(code)) {
                throw BusinessException("邮箱注册需要验证码")
            }
            val valid = verificationCodeService.verifyCode(email.orEmpty(), code.orEmpty())
            if (!valid) {
                throw BusinessException("验证码无效或已过期")
            }
        }

        userDomainService.register(email, phone, password)
    }

    fun sendEmailVerificationCode(email: String, captchaUuid: String, captchaCode: String, ip: String) {
        userDomainService.checkAccountExist(email, null)
        verificationCodeService.generateCode(email, captchaUuid, captchaCode, ip)
            .also { emailService.sendVerificationCode(email, it) }
    }

    fun sendResetPasswordCode(email: String, captchaUuid: String, captchaCode: String, ip: String) {
        userDomainService.findUserByAccount(email) ?: throw BusinessException("该邮箱未注册")
        verificationCodeService.generateCode(
            email,
            captchaUuid,
            captchaCode,
            ip,
            VerificationCodeService.BUSINESS_TYPE_RESET_PASSWORD
        ).also { emailService.sendVerificationCode(email, it) }
    }

    fun verifyEmailCode(email: String, code: String): Boolean = verificationCodeService.verifyCode(email, code)

    fun verifyResetPasswordCode(email: String, code: String): Boolean =
        verificationCodeService.verifyCode(email, code, VerificationCodeService.BUSINESS_TYPE_RESET_PASSWORD)

    fun resetPassword(email: String, newPassword: String, code: String) {
        if (!verifyResetPasswordCode(email, code)) {
            throw BusinessException("验证码无效或已过期")
        }
        val user = userDomainService.findUserByAccount(email) ?: throw BusinessException("用户不存在")
        userDomainService.updatePassword(user.id ?: throw BusinessException("用户不存在"), newPassword)
    }
}
