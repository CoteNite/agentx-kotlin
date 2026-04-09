package cn.cotenite.interfaces.dto.tool.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * @author  yhk
 * Description  
 * Date  2026/4/8 17:31
 */
data class CreateToolRequest(
    /** 工具名称 */
    @field:NotBlank(message = "工具名称不可为空")
    var name: String? = null,

    /** 工具图标 */
    var icon: String? = null,

    /** 副标题 */
    @field:NotBlank(message = "副标题不可为空")
    var subtitle: String? = null,

    /** 工具描述 */
    @field:NotBlank(message = "工具描述不可为空")
    var description: String? = null,

    /** 标签 */
    @field:NotEmpty(message = "标签不可为空")
    var labels: List<String>? = null,

    /** 上传地址 */
    @field:NotBlank(message = "上传地址不可为空")
    var uploadUrl: String? = null,

    /** 安装命令 */
    @field:NotNull(message = "安装命令不可为空")
    var installCommand: Map<String, Any>? = null
)
