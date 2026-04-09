package cn.cotenite.domain.tool.model.config

/** 工具定义 */
data class ToolDefinition(
    /** 工具名称 */
    var name: String? = null,

    /** 工具描述 */
    var description: String? = null,

    /** 参数定义 */
    var parameters: Map<String, Any?>? = null,

    /** 是否启用 */
    var enabled: Boolean? = null
)