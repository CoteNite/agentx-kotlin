package cn.cotenite.infrastructure.auth

import kotlinx.coroutines.asContextElement
import kotlin.coroutines.CoroutineContext

class ExternalApiContext {
    private val userIdLocal = ThreadLocal<String?>()
    private val agentIdLocal = ThreadLocal<String?>()

    var userId: String?
        get() = userIdLocal.get()
        set(value) = userIdLocal.set(value)

    var agentId: String?
        get() = agentIdLocal.get()
        set(value) = agentIdLocal.set(value)

    fun hasUserId(): Boolean = userId != null
    fun hasAgentId(): Boolean = agentId != null

    /**
     * 清理上下文，避免内存泄漏
     */
    fun clear() {
        userIdLocal.remove()
        agentIdLocal.remove()
    }

    /**
     * 关键：适配协程
     * 将当前的 ThreadLocal 值封装为协程上下文元素
     */
    fun asCoroutineContext(): CoroutineContext {
        return userIdLocal.asContextElement() + agentIdLocal.asContextElement()
    }

}