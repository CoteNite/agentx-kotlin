package cn.cotenite.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/** S3对象存储配置属性 */
@Component
@ConfigurationProperties(prefix = "s3")
class S3Properties {

    /** S3服务端点 */
    var endpoint: String? = null

    /** 自定义域名(可选) */
    var customDomain: String? = null

    /** 访问密钥 */
    var accessKey: String? = null

    /** 密钥 */
    var secretKey: String? = null

    /** 默认存储桶名称 */
    var bucketName: String? = null

    /** 区域 */
    var region: String? = null

    /** 是否启用路径样式访问 */
    var pathStyleAccess: Boolean = true

    /** 文件访问URL前缀 */
    var urlPrefix: String? = null
}