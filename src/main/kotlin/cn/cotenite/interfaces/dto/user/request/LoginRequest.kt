package cn.cotenite.interfaces.dto.user.request

import jakarta.validation.constraints.NotBlank

/**
 * 登录请求
 */
data class LoginRequest(
    @field:NotBlank(message = "账号不能为空")
    var account: String? = null,
    @field:NotBlank(message = "密码不能为空")
    var password: String? = null
)
