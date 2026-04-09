package cn.cotenite.application.conversation.service.message.agent

import cn.cotenite.application.conversation.service.handler.context.ChatContext
import cn.cotenite.infrastructure.mcpGateway.MCPGatewayService
import cn.cotenite.infrastructure.utils.JsonUtils
import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.DefaultMcpClient
import dev.langchain4j.mcp.client.McpClient
import dev.langchain4j.mcp.client.transport.McpTransport
import dev.langchain4j.mcp.client.transport.PresetParameter
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport
import dev.langchain4j.service.tool.ToolProvider
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Agent 工具管理器
 */
@Component
class AgentToolManager(
    private val mcpGatewayService: MCPGatewayService
){

    /**
     * 创建工具提供者
     *
     * @param mcpServerNames 工具名称列表
     * @param toolPresetParams 工具预设参数 Map
     * @return 工具提供者实例，如果工具列表为空则返回 null
     */
    fun createToolProvider(
        mcpServerNames: List<String?>,
        toolPresetParams: Map<String?, MutableMap<String?, MutableMap<String?, String?>?>?>?
    ): ToolProvider? {
        if (mcpServerNames.isEmpty()) {
            return null
        }

        val mcpClients = mutableListOf<McpClient>()

        for (mcpServerName in mcpServerNames) {
            val sseUrl= this.mcpGatewayService.getSSEUrl(mcpServerName)
            val transport = HttpMcpTransport
                .Builder()
                .sseUrl(sseUrl)
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofHours(1)).build()

            val mcpClient: McpClient = DefaultMcpClient.Builder().transport(transport).build()

            /** 预先设置参数  */
            if (toolPresetParams != null && toolPresetParams.containsKey(mcpServerName)) {
                val presetParameters = ArrayList<PresetParameter?>()
                for (key in toolPresetParams.keys) {
                    toolPresetParams[key]!!.forEach { (k: String?, v: Map<String?, String?>?) ->
                        presetParameters.add(PresetParameter(k, JsonUtils.toJsonString(v)))
                    }
                }
                mcpClient.presetParameters(presetParameters)
            }
            mcpClients.add(mcpClient)
        }


        return McpToolProvider.builder()
            .mcpClients(mcpClients)
            .build()
    }

    fun getAvailableTools(chatContext: ChatContext): List<String?> = chatContext.mcpServerName
}
