package cn.cotenite.agentxkotlin.infrastructure.llm.config

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProviderConfig(
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    val customHeaders : Map<String, String>,
)