package cn.cotenite.application.tool.dto

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.constant.ToolType
import cn.cotenite.domain.tool.constant.UploadType
import cn.cotenite.domain.tool.model.config.ToolDefinition
import java.time.LocalDateTime

data class ToolDTO(
    var id: String? = null,
    var name: String? = null,
    var icon: String? = null,
    var subtitle: String? = null,
    var description: String? = null,
    var userId: String? = null,
    var userName: String? = null, // 作者名称
    var labels: List<String>? = null,
    var toolType: ToolType? = null,
    var uploadType: UploadType? = null,
    var uploadUrl: String? = null,
    var toolList: List<ToolDefinition>? = null,
    var status: ToolStatus? = null, // 后续可能删除
    var isOffice: Boolean? = null,
    var installCount: Int? = null, // 安装数量
    var currentVersion: String? = null, // 当前版本号
    var installCommand: String? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var rejectReason: String? = null,
    var failedStepStatus: ToolStatus? = null,
    var mcpServerName: String? = null
)