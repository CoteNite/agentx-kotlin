package cn.cotenite.infrastructure.auth

/**
 * 用户上下文
 * 用于存储当前线程的用户信息
 */
object UserContext {

    private val currentUserId = ThreadLocal<String>()

    /**
     * 设置当前用户ID
     */
    fun setCurrentUserId(userId: String) =
        currentUserId.set(userId)

    /**
     * 获取当前用户ID
     */
    fun getCurrentUserId(): String? = currentUserId.get()

    /**
     * 清除当前用户信息
     */
    fun clear() =
        currentUserId.remove()
}
