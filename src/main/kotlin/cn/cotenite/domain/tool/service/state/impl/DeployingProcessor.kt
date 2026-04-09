package cn.cotenite.domain.tool.service.state.impl

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.service.state.ToolStateProcessor
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.mcpGateway.MCPGatewayService
import cn.cotenite.infrastructure.utils.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author  yhk
 * Description
 * Date  2026/4/7 15:25
 */
class DeployingProcessor(
    private val mcpGatewayService: MCPGatewayService
): ToolStateProcessor{


    private val logger: Logger = LoggerFactory.getLogger(DeployingProcessor::class.java)

    override fun getStatus()= ToolStatus.DEPLOYING

    override suspend fun process(tool: ToolEntity) {
        logger.info("工具ID: {} 进入DEPLOYING状态，开始部署。", tool.id)
        try {
            // 获取安装命令
            val installCommand = tool.installCommand
            if (installCommand.isNullOrEmpty()) {
                throw BusinessException("工具ID: " + tool.id + " 安装命令为空，无法部署。")
            }
            val installCommandJson = JsonUtils.toJsonString(installCommand)

            // 调用MCPGatewayService进行部署
            val deploySuccess: Boolean = mcpGatewayService.deployTool(installCommandJson)

            if (deploySuccess) {
                logger.info("工具部署成功，工具ID: {}", tool.id)
            } else {
                logger.error(
                    "工具部署失败 (API returned non-success status)，工具ID: {}", tool.id)
                throw BusinessException("MCP Gateway部署返回非成功状态。")
            }
        } catch (e: BusinessException) {
            logger.error("部署工具 {} (ID: {}) 失败: {}", tool.name, tool.id, e.message, e)
            throw e
        } catch (e: Exception) { logger.error("部署工具 {} (ID: {}) 过程中发生意外错误: {}", tool.name, tool.id, e.message, e)
            throw BusinessException("部署工具过程中发生意外错误: " + e.message, e)
        }
    }

    override fun getNextStatus()=ToolStatus.FETCHING_TOOLS
}