package cn.cotenite.interfaces.dto.user.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 重置密码请求
 */
data class ResetPasswordRequest(
    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    var email: String? = null,
    @field:NotBlank(message = "新密码不能为空")
    @field:Size(min = 6, max = 20, message = "密码长度应在6-20位之间")
    var newPassword: String? = null,
    @field:NotBlank(message = "验证码不能为空")
    var code: String? = null
)
