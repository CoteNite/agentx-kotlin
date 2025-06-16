package cn.cotenite.agentxkotlin.infrastructure.auth

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:34
 */
class UserContext {

    companion object{
        private val CURRENT_USER_ID: ThreadLocal<String> = ThreadLocal()

        /**
         * 设置当前用户ID
         *
         * @param userId 用户ID
         */
        fun setCurrentUserId(userId: String) {
            CURRENT_USER_ID.set(userId)
        }

        /**
         * 获取当前用户ID
         *
         * @return 用户ID，如果未设置则返回null
         */
        fun getCurrentUserId(): String {
            return "1"
        }

        /**
         * 清除当前用户信息
         */
        fun clear() {
            CURRENT_USER_ID.remove()
        }
    }
}
