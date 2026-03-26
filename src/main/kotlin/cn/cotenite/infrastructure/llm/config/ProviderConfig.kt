package cn.cotenite.infrastructure.llm.config

import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * 基础设施层服务商配置
 */
data class ProviderConfig(
    val apiKey: String?,
    val baseUrl: String?,
    var model: String?,
    var protocol: ProviderProtocol,
    var customHeaders: MutableMap<String, String> = mutableMapOf()
) {
    fun addCustomHeader(key: String, value: String) {
        customHeaders[key] = value
    }
}
