package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.common.util.ValidationUtils

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 14:08
 */
data class UpdateAgentStatusRequest(
    val enabled: Boolean,
){
    /**
     * 校验请求参数
     */
    fun validate() {
        ValidationUtils.notNull(enabled, "enabled")
    }

}
