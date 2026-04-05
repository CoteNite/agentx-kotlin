package cn.cotenite.infrastructure.verification.storage

/**
 * 验证码存储接口
 */
interface CodeStorage {

    fun storeCode(key: String, code: String, expirationMillis: Long)

    fun verifyCode(key: String, code: String): Boolean
}
