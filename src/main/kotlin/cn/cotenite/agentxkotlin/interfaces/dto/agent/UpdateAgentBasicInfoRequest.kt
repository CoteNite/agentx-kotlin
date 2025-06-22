package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.infrastructure.util.ValidationUtils

/**
 * 更新Agent基本信息的请求对象
 */
data class UpdateAgentBasicInfoRequest(
    var name: String? = null,
    var avatar: String? = null,
    var description: String? = null
) {
    /**
     * 校验请求参数
     */
    fun validate() {
        ValidationUtils.notEmpty(name, "name")
        ValidationUtils.length(name, 1, 50, "name")
    }
}