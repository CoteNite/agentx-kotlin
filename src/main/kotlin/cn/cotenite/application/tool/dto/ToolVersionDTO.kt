package cn.cotenite.application.tool.dto

import cn.cotenite.domain.tool.model.config.ToolDefinition
import java.time.LocalDateTime

/**
 * 工具版本 DTO
 */
data class ToolVersionDTO(
    var id: String? = null,
    var name: String? = null,
    var icon: String? = null,
    var subtitle: String? = null,
    var description: String? = null,
    var userId: String? = null,
    var version: String? = null,
    var toolId: String? = null,
    var uploadType: String? = null,
    var uploadUrl: String? = null,
    var toolList: List<ToolDefinition>? = null,
    var labels: List<String>? = null,
    var isOffice: Boolean? = null,
    var publicStatus: Boolean? = null,
    var changeLog: String? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null,
    var userName: String? = null,
    var versions: List<ToolVersionDTO>? = null,
    var installCount: Long? = null,
    var mcpServerName: String? = null
)