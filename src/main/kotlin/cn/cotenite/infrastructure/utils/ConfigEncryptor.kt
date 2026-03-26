package cn.cotenite.infrastructure.utils

/**
 * 配置加解密器
 */
object ConfigEncryptor {

    fun encrypt(plain: String): String = EncryptUtils.encrypt(plain) ?: ""

    fun decrypt(cipher: String): String = EncryptUtils.decrypt(cipher) ?: ""
}
