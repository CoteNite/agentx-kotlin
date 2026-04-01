package cn.cotenite.application.conversation.service.message.agent

import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.DefaultMcpClient
import dev.langchain4j.mcp.client.McpClient
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport
import dev.langchain4j.service.tool.ToolProvider
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * @author  yhk
 * Description  
 * Date  2026/3/29 19:16
 */
@Component
class AgentToolManager {


    fun createToolProvider(toolUrls: List<String>): ToolProvider{
        val mcpClients=mutableListOf<McpClient>()
        toolUrls.takeIf { it.isNotEmpty() }.let {
            toolUrls.forEach { toolUrl ->
                val transport= StreamableHttpMcpTransport.Builder()
                    .url(toolUrl)
                    .logRequests(true)
                    .logResponses(true)
                    .timeout(Duration.ofHours(1))
                    .build()
                val mcpClient = DefaultMcpClient.Builder()
                    .transport(transport)
                    .build()
                mcpClients.add(mcpClient)
            }
        }

        return McpToolProvider.builder()
            .mcpClients(mcpClients)
            .build()
    }


    fun getAvailableTools(): List<String>{
        return listOf()
    }

}