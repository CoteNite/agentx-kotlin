package cn.cotenite.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * 高可用网关配置属性类，用于集中管理所有与高可用网关相关的配置参数。
 *
 * @author yhk
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "high-availability")
class HighAvailabilityProperties {
    /**
     * 是否启用高可用功能。
     */
    var enabled: Boolean = false

    /**
     * 高可用网关基础 URL。
     */
    var gatewayUrl: String? = null
        get() = field?.plus("/api")

    /**
     * API 密钥。
     */
    var apiKey: String? = null

    /**
     * 连接超时时间(毫秒)，默认 30 秒。
     */
    var connectTimeout: Int = 30000

    /**
     * 读取超时时间(毫秒)，默认 60 秒。
     */
    var readTimeout: Int = 60000
}
