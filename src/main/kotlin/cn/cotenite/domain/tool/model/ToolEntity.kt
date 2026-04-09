package cn.cotenite.domain.tool.model

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.constant.ToolType
import cn.cotenite.domain.tool.constant.UploadType
import cn.cotenite.domain.tool.model.config.ToolDefinition
import cn.cotenite.infrastructure.converter.*
import cn.cotenite.infrastructure.entity.BaseEntity
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/** 工具实体类 */
@TableName(value = "tools", autoResultMap = true)
class ToolEntity : BaseEntity() {

    /** 工具唯一ID */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    var id: String? = null

    /** 工具名称 */
    @TableField("name")
    var name: String? = null

    /** 工具图标 */
    @TableField("icon")
    var icon: String? = null

    /** 副标题 */
    @TableField("subtitle")
    var subtitle: String? = null

    /** 工具描述 */
    @TableField("description")
    var description: String? = null

    /** 用户ID */
    @TableField("user_id")
    var userId: String? = null

    /** 标签列表 */
    @TableField(value = "labels", typeHandler = ListStringConverter::class)
    var labels: List<String>? = null

    /** 工具类型：mcp */
    @TableField(value = "tool_type", typeHandler = ToolTypeConverter::class)
    var toolType: ToolType = ToolType.MCP

    /** 上传方式：github, zip */
    @TableField(value = "upload_type", typeHandler = UploadTypeConverter::class)
    var uploadType: UploadType = UploadType.GITHUB

    /** 上传URL */
    @TableField("upload_url")
    var uploadUrl: String? = null

    /** 安装命令 */
    @TableField(value = "install_command", typeHandler = MapConverter::class)
    var installCommand: Map<String, Any>? = null

    /** 工具列表 */
    @TableField(value = "tool_list", typeHandler = ToolDefinitionListConverter::class)
    var toolList: List<ToolDefinition>? = null

    /** 审核状态 */
    @TableField(value = "status", typeHandler = ToolStatusConverter::class)
    var status: ToolStatus? = null

    /** 是否官方工具 */
    @TableField("is_office")
    var isOffice: Boolean? = null

    /** 拒绝原因 */
    @TableField("reject_reason")
    var rejectReason: String? = null

    /** 失败步骤状态 */
    @TableField(value = "failed_step_status", typeHandler = ToolStatusConverter::class)
    var failedStepStatus: ToolStatus? = null

    /** MCP服务器名称 */
    @TableField("mcp_server_name")
    var mcpServerName: String? = null

}