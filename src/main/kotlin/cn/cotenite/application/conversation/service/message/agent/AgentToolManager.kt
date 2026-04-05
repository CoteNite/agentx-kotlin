package cn.cotenite.application.conversation.service.message.agent

import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.DefaultMcpClient
import dev.langchain4j.mcp.client.McpClient
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport
import dev.langchain4j.service.tool.ToolProvider
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Agent 工具管理器
 */
@Component
class AgentToolManager {

    fun createToolProvider(toolUrls: List<String>?): ToolProvider? {
        if (toolUrls.isNullOrEmpty()) {
            return null
        }

        val mcpClients = mutableListOf<McpClient>()
        toolUrls.forEach { toolUrl ->
            val transport = HttpMcpTransport.builder()
                .sseUrl(toolUrl)
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofHours(1))
                .build()

            val mcpClient = DefaultMcpClient.Builder()
                .transport(transport)
                .build()
            mcpClients.add(mcpClient)
        }

        return McpToolProvider.builder()
            .mcpClients(mcpClients)
            .build()
    }

    fun getAvailableTools(): List<String> = listOf("http://localhost:8005/sse?api_key=123456")
}
