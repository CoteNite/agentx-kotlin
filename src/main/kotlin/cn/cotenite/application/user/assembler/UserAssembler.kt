package cn.cotenite.application.user.assembler

import cn.cotenite.application.user.dto.UserDTO
import cn.cotenite.domain.user.model.UserEntity
import cn.cotenite.interfaces.dto.user.request.RegisterRequest
import cn.cotenite.interfaces.dto.user.request.UserUpdateRequest

/**
 * 用户对象转换器
 */
object UserAssembler {

    fun toDTO(userEntity: UserEntity?): UserDTO? = userEntity?.let {
        UserDTO(
            id = it.id,
            nickname = it.nickname,
            email = it.email,
            phone = it.phone
        )
    }

    fun toEntity(userDTO: UserDTO): UserEntity = UserEntity().apply {
        id = userDTO.id
        nickname = userDTO.nickname
        email = userDTO.email
        phone = userDTO.phone
    }

    fun toEntity(registerRequest: RegisterRequest): UserEntity = UserEntity().apply {
        email = registerRequest.email
        phone = registerRequest.phone
        password = registerRequest.password
    }

    fun toEntity(userUpdateRequest: UserUpdateRequest, userId: String): UserEntity = UserEntity().apply {
        id = userId
        nickname = userUpdateRequest.nickname
    }
}
