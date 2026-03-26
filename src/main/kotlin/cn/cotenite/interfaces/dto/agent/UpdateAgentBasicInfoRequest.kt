package cn.cotenite.interfaces.dto.agent

import cn.cotenite.infrastructure.exception.ParamValidationException

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
        val agentName = name ?: throw ParamValidationException("name", "name不能为空")
        if (agentName.isBlank()) throw ParamValidationException("name", "name不能为空")
        if (agentName.length !in 1..50) {
            throw ParamValidationException("name", "name长度必须在1到50之间")
        }
    }
}
