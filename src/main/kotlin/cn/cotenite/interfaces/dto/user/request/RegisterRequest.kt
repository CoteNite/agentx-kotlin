package cn.cotenite.interfaces.dto.user.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * 注册请求
 */
data class RegisterRequest(
    @field:Email(message = "不是一个合法的邮箱")
    var email: String? = null,
    var phone: String? = null,
    @field:NotBlank(message = "密码不能为空")
    var password: String? = null,
    var code: String? = null
)
