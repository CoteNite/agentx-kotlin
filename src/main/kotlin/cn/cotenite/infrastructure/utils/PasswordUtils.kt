package cn.cotenite.infrastructure.utils

import cn.hutool.crypto.digest.BCrypt

/**
 * 密码工具类
 */
object PasswordUtils {

    fun encode(rawPassword: String): String = BCrypt.hashpw(rawPassword)

    fun matches(rawPassword: String, encodedPassword: String): Boolean =
        BCrypt.checkpw(rawPassword, encodedPassword)
}
