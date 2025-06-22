package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.infrastructure.util.ValidationUtils

/**
 * 更新Agent状态的请求对象
 */
data class UpdateAgentStatusRequest(
    var enabled: Boolean? = null
) {
    /**
     * 校验请求参数
     */
    fun validate() {
        ValidationUtils.notNull(enabled, "enabled")
    }
}