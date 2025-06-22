package cn.cotenite.agentxkotlin.infrastructure.util

import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:12
 */
object EncryptUtils {

    private const val ALGORITHM = "AES"
    private const val SECRET_KEY = "1234567890123456" // 16位密钥

    /**
     * 加密字符串
     *
     * @param data 待加密的字符串
     * @return 加密后的字符串
     * @throws RuntimeException 如果加密失败
     */
    fun encrypt(data: String?): String? { // 将参数和返回类型都设为可空
        if (data == null) {
            return null
        }
        return try {
            val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            Base64.getEncoder().encodeToString(encryptedBytes)
        } catch (e: Exception) {
            throw RuntimeException("加密失败: ${e.message}", e)
        }
    }

    /**
     * 解密字符串
     *
     * @param encryptedData 已加密的字符串
     * @return 解密后的字符串
     * @throws RuntimeException 如果解密失败
     */
    fun decrypt(encryptedData: String?): String? { // 将参数和返回类型都设为可空
        if (encryptedData == null) {
            return null
        }
        return try {
            val secretKey = SecretKeySpec(SECRET_KEY.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData))
            String(decryptedBytes)
        } catch (e: Exception) {
            throw RuntimeException("解密失败: ${e.message}", e)
        }
    }
}