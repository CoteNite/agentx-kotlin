package cn.cotenite.infrastructure.verification.storage

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 内存验证码存储实现
 */
@Component
class MemoryCodeStorage : CodeStorage {

    private data class StoredCode(
        val code: String,
        val expirationTime: Long
    )

    private val storage = ConcurrentHashMap<String, StoredCode>()

    override fun storeCode(key: String, code: String, expirationMillis: Long) {
        storage[key] = StoredCode(code, System.currentTimeMillis() + expirationMillis)
    }

    override fun verifyCode(key: String, code: String): Boolean {
        val storedCode = storage[key] ?: return false
        if (System.currentTimeMillis() > storedCode.expirationTime) {
            storage.remove(key)
            return false
        }
        val matched = storedCode.code.equals(code, ignoreCase = true)
        if (matched) {
            storage.remove(key)
        }
        return matched
    }
}
