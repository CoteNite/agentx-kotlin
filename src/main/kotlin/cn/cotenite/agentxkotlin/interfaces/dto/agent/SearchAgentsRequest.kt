package cn.cotenite.agentxkotlin.interfaces.dto.agent

/**
 * 搜索Agent的请求对象
 */
data class SearchAgentsRequest(
    var name: String? = null
)