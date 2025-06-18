package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.agent.model.PublishStatus
import cn.cotenite.agentxkotlin.domain.common.exception.ParamValidationException
import cn.cotenite.agentxkotlin.domain.common.util.ValidationUtils


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 14:04
 */
data class ReviewAgentVersionRequest(
    val status: PublishStatus,
    var rejectReason: String?=null
){

    fun validate() {
        ValidationUtils.notNull(status, "status")

        if (PublishStatus.REJECTED == status && (rejectReason == null || rejectReason?.trim { it <= ' ' }?.isEmpty() != false)
        ) {
            throw ParamValidationException("rejectReason", "拒绝时必须提供拒绝原因")
        }

    }

}
