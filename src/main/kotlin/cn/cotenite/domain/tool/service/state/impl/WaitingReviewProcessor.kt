package cn.cotenite.domain.tool.service.state.impl

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.service.state.ToolStateProcessor

/**
 * @author  yhk
 * Description  
 * Date  2026/4/7 22:43
 */
class WaitingReviewProcessor: ToolStateProcessor {
    override fun getStatus()= ToolStatus.WAITING_REVIEW

    override suspend fun process(tool: ToolEntity) {

    }

    override fun getNextStatus()=ToolStatus.GITHUB_URL_VALIDATE
}