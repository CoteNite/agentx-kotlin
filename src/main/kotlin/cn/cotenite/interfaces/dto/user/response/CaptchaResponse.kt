package cn.cotenite.interfaces.dto.user.response

/**
 * 图形验证码响应
 */
data class CaptchaResponse(
    var uuid: String? = null,
    var imageBase64: String? = null
)
