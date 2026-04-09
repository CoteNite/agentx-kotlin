package cn.cotenite.domain.tool.service.state.impl

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.service.state.ToolStateProcessor
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.mcpGateway.MCPGatewayService
import org.slf4j.LoggerFactory
/**
 * @author  yhk
 * Description  
 * Date  2026/4/7 18:25
 */
class FetchingToolsProcessor (
    private val mcpGatewayService: MCPGatewayService
): ToolStateProcessor {

    private val logger = LoggerFactory.getLogger(FetchingToolsProcessor::class.java)

    override fun getStatus()= ToolStatus.FETCHING_TOOLS

    override suspend fun process(tool: ToolEntity) {
        logger.info("工具ID: ${tool.id} 进入 FETCHING_TOOLS 状态，开始获取工具列表。")

        try {
            // 1. 获取安装命令
            val installCommand = tool.installCommand?.takeIf { it.isNotEmpty() } ?: throw BusinessException("安装命令为空")

            // 2. 解析 mcpServers 并获取第一个 key 作为工具名称
            @Suppress("UNCHECKED_CAST")
            val mcpServers = installCommand["mcpServers"] as? Map<String, Any>

            val toolName = mcpServers?.keys?.firstOrNull()?.takeIf { it.isNotBlank() } ?: throw BusinessException("工具ID: ${tool.id} 无法从安装命令中获取有效的 mcpServers 工具名称。")

            logger.info("从 MCP Gateway 获取工具 $toolName 的列表")

            // 3. 调用 MCPGatewayService 获取工具列表
            val toolDefinitions = mcpGatewayService.listTools(toolName)

            // 4. 将获取到的工具定义列表设置到 ToolEntity 中
            tool.toolList = toolDefinitions

            logger.info("成功获取到工具 $toolName 的列表，共 ${toolDefinitions.size} 个定义。")

        } catch (e: BusinessException) {
            logger.error("获取工具列表失败 ${tool.name} (ID: ${tool.id}): ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("获取工具列表 ${tool.name} (ID: ${tool.id}) 过程中发生意外错误: ${e.message}", e)
            throw BusinessException("获取工具列表过程中发生意外错误: ${e.message}", e)
        }
    }

    override fun getNextStatus()=ToolStatus.MANUAL_REVIEW
}