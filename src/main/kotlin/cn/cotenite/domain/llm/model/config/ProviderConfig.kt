package cn.cotenite.domain.llm.model.config

/**
 * 服务商配置
 */
data class ProviderConfig(
    /**
     * API Key
     */
    var apiKey: String? = null,
    /**
     * Base URL
     */
    var baseUrl: String? = null,
    /**
     * 其他扩展配置
     */
    var extras: MutableMap<String, Any?> = mutableMapOf()
)
