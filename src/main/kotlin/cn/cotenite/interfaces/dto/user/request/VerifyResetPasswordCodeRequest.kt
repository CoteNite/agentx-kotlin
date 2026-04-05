package cn.cotenite.interfaces.dto.user.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * 校验重置密码验证码请求
 */
data class VerifyResetPasswordCodeRequest(
    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    var email: String? = null,
    @field:NotBlank(message = "验证码不能为空")
    var code: String? = null
)
