package cn.cotenite.domain.user.service

import cn.cotenite.domain.user.model.UserEntity
import cn.cotenite.domain.user.repository.UserRepository
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.utils.PasswordUtils
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import org.springframework.stereotype.Service
import java.util.*

/**
 * 用户领域服务
 */
@Service
class UserDomainService(
    private val userRepository: UserRepository
) {

    fun getUserInfo(id: String): UserEntity? = userRepository.selectById(id)

    fun findUserByAccount(account: String): UserEntity? =
        userRepository.selectOne(
            KtQueryWrapper(UserEntity::class.java)
                .eq(UserEntity::email, account)
                .or()
                .eq(UserEntity::phone, account)
        )

    fun findUserByGithubId(githubId: String): UserEntity? =
        userRepository.selectOne(
            KtQueryWrapper(UserEntity::class.java)
                .eq(UserEntity::githubId, githubId)
        )

    fun register(email: String?, phone: String?, password: String): UserEntity =
        UserEntity().apply {
            this.email = email
            this.phone = phone
            this.password = PasswordUtils.encode(password)
            nickname = generateNickname()
            valid()
            checkAccountExist(this.email)
            userRepository.checkInsert(this)
        }

    fun encryptPassword(password: String): String = PasswordUtils.encode(password)

    fun login(account: String, password: String): UserEntity {
        val userEntity = findUserByAccount(account)
        if (userEntity == null || !PasswordUtils.matches(password, userEntity.password.orEmpty())) {
            throw BusinessException("账号密码错误")
        }
        return userEntity
    }

    fun checkAccountExist(email: String?) {
        val exists = userRepository.selectOne(
            KtQueryWrapper(UserEntity::class.java)
                .eq(email != null, UserEntity::email, email)
        ) != null
        if (exists) {
            throw BusinessException("账号已存在,不可重复账注册")
        }
    }

    fun updateUserInfo(user: UserEntity) {
        userRepository.checkedUpdateById(user)
    }

    fun updatePassword(userId: String, newPassword: String) {
        val user = userRepository.selectById(userId) ?: throw BusinessException("用户不存在")
        user.password = PasswordUtils.encode(newPassword)
        userRepository.checkedUpdateById(user)
    }

    fun getByIds(userIds: MutableList<String?>?): List<UserEntity?> {
        if (userIds.isNullOrEmpty()) {
            return emptyList()
        }
        return userRepository.selectByIds(userIds)
    }

    private fun generateNickname(): String =
        "agent-x${UUID.randomUUID().toString().replace("-", "").take(6)}"
}
