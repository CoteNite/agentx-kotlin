package cn.cotenite.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * @author  yhk
 * Description  
 * Date  2026/4/7 15:27
 */
@Configuration
@ConfigurationProperties(prefix = "mcp.gateway")
class MCPGatewayProperties {

    var baseUrl: String? = null // 网关基础URL
    var apiKey: String? = null // API密钥
    var connectTimeout = 30000 // 连接超时(毫秒)，默认30秒
    var readTimeout = 60000 // 读取超时(毫秒)，默认60秒
}