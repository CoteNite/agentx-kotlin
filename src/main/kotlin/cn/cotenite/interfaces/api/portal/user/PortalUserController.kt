package cn.cotenite.interfaces.api.portal.user

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.application.user.dto.UserDTO
import cn.cotenite.application.user.service.UserAppService
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.user.request.UserUpdateRequest

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
class PortalUserController(
    private val userAppService: UserAppService
) {

    @GetMapping
    fun getUserInfo(): Result<UserDTO?> =
        Result.success(userAppService.getUserInfo(currentUserId()))

    @PostMapping
    fun updateUserInfo(@RequestBody @Validated userUpdateRequest: UserUpdateRequest): Result<Void> {
        userAppService.updateUserInfo(userUpdateRequest, currentUserId())
        return Result.success()
    }

    private fun currentUserId(): String = UserContext.getCurrentUserId()
}
