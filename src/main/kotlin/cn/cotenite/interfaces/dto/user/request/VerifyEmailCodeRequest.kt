package cn.cotenite.interfaces.dto.user.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * 校验注册验证码请求
 */
data class VerifyEmailCodeRequest(
    @field:Email(message = "不是一个合法的邮箱")
    @field:NotBlank(message = "邮箱不能为空")
    var email: String? = null,
    @field:NotBlank(message = "验证码不能为空")
    var code: String? = null
)
