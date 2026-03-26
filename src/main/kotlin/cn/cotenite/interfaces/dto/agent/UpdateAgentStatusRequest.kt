package cn.cotenite.interfaces.dto.agent

import cn.cotenite.infrastructure.exception.ParamValidationException

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
        enabled ?: throw ParamValidationException("enabled", "enabled不能为空")
    }
}
