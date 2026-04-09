package cn.cotenite.domain.tool.service.state.impl

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.service.state.ToolStateProcessor

/**
 * 人工审核状态处理器
 */
class ManualReviewProcessor : ToolStateProcessor {

    override fun getStatus() = ToolStatus.MANUAL_REVIEW


    override suspend fun process(tool: ToolEntity) {
    }

    override fun getNextStatus():ToolStatus? =null
}