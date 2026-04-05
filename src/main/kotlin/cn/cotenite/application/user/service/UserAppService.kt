package cn.cotenite.application.user.service

import org.springframework.stereotype.Service
import cn.cotenite.application.user.assembler.UserAssembler
import cn.cotenite.application.user.dto.UserDTO
import cn.cotenite.domain.user.service.UserDomainService
import cn.cotenite.interfaces.dto.user.request.UserUpdateRequest

/**
 * 用户应用服务
 */
@Service
class UserAppService(
    private val userDomainService: UserDomainService
) {

    fun getUserInfo(id: String): UserDTO? =
        userDomainService.getUserInfo(id).let(UserAssembler::toDTO)

    fun updateUserInfo(userUpdateRequest: UserUpdateRequest, userId: String) {
        UserAssembler.toEntity(userUpdateRequest, userId)
            .let(userDomainService::updateUserInfo)
    }
}
