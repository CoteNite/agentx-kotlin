package cn.cotenite.infrastructure.utils

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 加解密工具类
 */
object EncryptUtils {

    private const val ALGORITHM = "AES"
    private const val SECRET_KEY = "1234567890123456"

    fun encrypt(data: String?): String? = runCatching {
        data?.let {
            val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(it.toByteArray(StandardCharsets.UTF_8))
            Base64.getEncoder().encodeToString(encryptedBytes)
        }
    }.getOrElse { throw RuntimeException("加密失败${it.message}", it) }

    fun decrypt(encryptedData: String?): String? = runCatching {
        encryptedData?.let {
            val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(it))
            String(decryptedBytes, StandardCharsets.UTF_8)
        }
    }.getOrElse { throw RuntimeException("解密失败:${it.message}", it) }
}
