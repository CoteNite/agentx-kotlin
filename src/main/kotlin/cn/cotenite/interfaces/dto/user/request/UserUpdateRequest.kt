package cn.cotenite.interfaces.dto.user.request

import jakarta.validation.constraints.NotBlank

/**
 * 用户更新请求
 */
data class UserUpdateRequest(
    @field:NotBlank(message = "昵称不可未空")
    var nickname: String? = null
)
