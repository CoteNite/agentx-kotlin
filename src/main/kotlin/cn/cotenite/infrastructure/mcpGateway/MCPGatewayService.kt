package cn.cotenite.infrastructure.mcpGateway

import cn.cotenite.domain.tool.model.config.ToolDefinition
import cn.cotenite.domain.tool.model.config.ToolSpecificationConverter
import cn.cotenite.infrastructure.config.MCPGatewayProperties
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.utils.JsonUtils
import dev.langchain4j.mcp.client.DefaultMcpClient
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.delay
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.HttpClientResponseHandler
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import org.apache.hc.core5.util.Timeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * @author  yhk
 * Description  
 * Date  2026/4/7 15:27
 */
@Service
class MCPGatewayService(
    private val properties: MCPGatewayProperties
){

    val logger: Logger = LoggerFactory.getLogger(MCPGatewayService::class.java)


    /** 初始化时验证配置有效性  */
    @PostConstruct
    fun init() {
        if (properties.baseUrl == null || properties.baseUrl!!.trim().isEmpty()) {
            logger.warn("MCP Gateway基础URL未配置 (mcp.gateway.base-url)")
        }

        if (properties.apiKey == null || properties.apiKey!!.trim().isEmpty()) {
            logger.warn("MCP Gateway API密钥未配置 (mcp.gateway.api-key)")
        }

        logger.info("MCP Gateway服务已初始化，基础URL: {}", properties.baseUrl)
    }

    fun getSSEUrl(mcpServerName: String?): String {
        return properties.baseUrl + "/" + mcpServerName + "/sse/sse?api_key=" + properties.apiKey
    }


    fun deployTool(installCommand: String): Boolean {
        val url = "${properties.baseUrl}/deploy"
        val httpClient = createHttpClient()

        // 1. 定义响应处理器 (符合源码中推荐的 HttpClientResponseHandler)
        val responseHandler = HttpClientResponseHandler{ response ->
            val statusCode = response.code
            val entity = response.entity
            val responseBody = entity?.let { EntityUtils.toString(it, StandardCharsets.UTF_8) }

            if (statusCode in 200..299 && responseBody != null) {
                val result = JsonUtils.parseObject(responseBody, MutableMap::class.java)
                logger.info("MCP Gateway 部署响应: $result")
                (result?.get("success") as? Boolean) ?: false
            } else {
                val errorMsg = "MCP Gateway 部署失败，状态码: $statusCode，响应: $responseBody"
                logger.error(errorMsg)
                throw BusinessException(errorMsg)
            }
        }

        // 2. 执行请求
        return httpClient.use { httpClient ->
            val httpPost = HttpPost(url).apply {
                addHeader("Authorization", "Bearer ${properties.apiKey}")
                entity = StringEntity(installCommand, ContentType.APPLICATION_JSON)
            }

            logger.info("发送部署请求到 MCP Gateway: $url")

            httpClient.execute(httpPost, responseHandler)
        }
    }

    /**
     * 从MCP Gateway获取工具列表
     * 使用 suspend 关键字，配合协程非阻塞等待
     */
    suspend fun listTools(toolName: String): List<ToolDefinition> {
        // 使用协程的 delay 替代 Thread.sleep，不阻塞线程
        delay(10000)

        val url = "${properties.baseUrl}/$toolName/sse/sse?api_key=${properties.apiKey}"

        val transport = HttpMcpTransport.Builder()
            .sseUrl(url)
            .timeout(Duration.ofSeconds(10))
            .logRequests(false)
            .logResponses(true)
            .build()

        val client = DefaultMcpClient.Builder().transport(transport).build()

        return try {
            val toolSpecifications = client.listTools()
            ToolSpecificationConverter.convert(toolSpecifications)
        } catch (e: Exception) {
            logger.error("调用MCP Gateway API失败", e)
            throw BusinessException("调用MCP Gateway API失败: ${e.message}", e)
        } finally {
            client.close()
        }
    }

    /** 创建配置了超时的HTTP客户端  */
    private fun createHttpClient(): CloseableHttpClient {
        // 注意：HttpClient 5 中使用 setResponseTimeout 替代了旧版的 setSocketTimeout
        val requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.of(properties.connectTimeout.toLong(), TimeUnit.MILLISECONDS))
            .setResponseTimeout(Timeout.of(properties.readTimeout.toLong(), TimeUnit.MILLISECONDS))
            .build()

        return HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build()
    }

}