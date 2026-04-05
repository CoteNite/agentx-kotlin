package cn.cotenite.interfaces.dto.user.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * 发送注册验证码请求
 */
data class SendEmailCodeRequest(
    @field:Email(message = "不是一个合法的邮箱")
    @field:NotBlank(message = "邮箱不能为空")
    var email: String? = null,
    @field:NotBlank(message = "验证码UUID不能为空")
    var captchaUuid: String? = null,
    @field:NotBlank(message = "图形验证码不能为空")
    var captchaCode: String? = null
)
