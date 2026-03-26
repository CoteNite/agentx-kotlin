package cn.cotenite.domain.agent.model

/**
 * Agent工具
 */
data class AgentTool(
    /**
     * 工具ID
     */
    var id: String? = null,
    /**
     * 工具名称
     */
    var name: String? = null,
    /**
     * 工具描述
     */
    var description: String? = null,
    /**
     * 工具类型
     */
    var type: String? = null,
    /**
     * 工具权限
     */
    var permissions: String? = null,
    /**
     * 工具配置
     */
    var config: Map<String, Any?>? = null
)
