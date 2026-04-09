package cn.cotenite.domain.tool.model.config

/**
 * 工具参数定义
 */
data class ToolParameter(
    var properties: Map<String, ParameterProperty>? = null,
    var required: List<String>? = null
)