package cn.cotenite.domain.tool.model

import cn.cotenite.domain.tool.constant.UploadType
import cn.cotenite.domain.tool.model.config.ToolDefinition
import cn.cotenite.infrastructure.converter.ListStringConverter
import cn.cotenite.infrastructure.converter.ToolDefinitionListConverter
import cn.cotenite.infrastructure.converter.UploadTypeConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/** 工具版本实体类 */
@TableName(value = "tool_versions", autoResultMap = true)
class ToolVersionEntity : BaseEntity() {

    /** 版本唯一ID */
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

    /** 版本号 */
    @TableField("version")
    var version: String? = null

    /** 工具ID */
    @TableField("tool_id")
    var toolId: String? = null

    /** 上传方式：github, zip */
    @TableField(value = "upload_type", typeHandler = UploadTypeConverter::class)
    var uploadType: UploadType? = null

    /** 上传URL */
    @TableField("upload_url")
    var uploadUrl: String? = null

    /** 工具列表 */
    @TableField(value = "tool_list", typeHandler = ToolDefinitionListConverter::class)
    var toolList: List<ToolDefinition>? = null

    /** 标签列表 */
    @TableField(value = "labels", typeHandler = ListStringConverter::class)
    var labels: List<String>? = null

    /** 是否官方工具 */
    @TableField("is_office")
    var isOffice: Boolean? = null

    /** 公开状态 */
    @TableField("public_status")
    var publicStatus: Boolean? = null

    /** 变更日志 */
    @TableField("change_log")
    var changeLog: String? = null

    /** MCP服务器名称 */
    @TableField("mcp_server_name")
    var mcpServerName: String? = null

}