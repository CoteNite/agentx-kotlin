package cn.cotenite.agentxkotlin.domain.llm.model.config

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:05
 */
data class ProviderConfig(
    var apiKey: String="",
    val baseUrl: String="",
)