package cn.cotenite.interfaces.dto.agent

import cn.cotenite.domain.agent.constant.PublishStatus
import cn.cotenite.infrastructure.exception.ParamValidationException

/**
 * 审核/更新Agent版本状态的请求对象
 */
data class ReviewAgentVersionRequest(
    var status: PublishStatus? = null,
    var rejectReason: String? = null
) {
    /**
     * 校验请求参数
     */
    fun validate() {
        val publishStatus = status ?: throw ParamValidationException("status", "状态不能为空")
        if (publishStatus == PublishStatus.REJECTED && rejectReason.isNullOrBlank()) {
            throw ParamValidationException("rejectReason", "拒绝时必须提供拒绝原因")
        }
    }
}
