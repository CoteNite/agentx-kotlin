package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.infrastructure.exception.ParamValidationException
import cn.cotenite.agentxkotlin.infrastructure.util.ValidationUtils


/**
 * 审核/更新Agent版本状态的请求对象
 */
data class ReviewAgentVersionRequest(
    /**
     * 目标状态: PUBLISHED-发布, REJECTED-拒绝, REMOVED-下架, REVIEWING-审核中
     */
    var status: PublishStatus? = null,

    /**
     * 拒绝原因，当status为REJECTED时必填
     */
    var rejectReason: String? = null
) {
    /**
     * 校验请求参数
     */
    fun validate() {
        ValidationUtils.notNull(status, "status")

        // 如果是拒绝，必须提供拒绝原因
        if (PublishStatus.REJECTED == status && rejectReason.isNullOrBlank()) {
            throw ParamValidationException("rejectReason", "拒绝时必须提供拒绝原因")
        }

        // 取消状态限制，允许所有合法的PublishStatus值
    }
}