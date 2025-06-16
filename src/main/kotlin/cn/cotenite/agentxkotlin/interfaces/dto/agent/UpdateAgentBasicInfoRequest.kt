package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.common.util.ValidationUtils

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 14:05
 */
data class UpdateAgentBasicInfoRequest(
    val name: String,
    val avatar:String,
    val description: String,
){

    fun validate() {
        ValidationUtils.notEmpty(name, "name")
        ValidationUtils.length(name, 1, 50, "name")
    }
}
