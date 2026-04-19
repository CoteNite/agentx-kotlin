package cn.cotenite.infrastructure.storage

import cn.cotenite.infrastructure.config.S3Properties
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Date

/**
 * OSS上传服务
 * 使用 S3 预签名 PUT URL 实现前端直传，兼容 MinIO / AWS S3 / 阿里云 OSS
 */
@Service
class OssUploadService(private val s3Properties: S3Properties) {

    /** 预签名 URL 有效期（秒） */
    private val presignExpireSeconds = 300L

    private val presigner: S3Presigner by lazy { createPresigner() }

    private val s3Client: S3Client by lazy { createS3Client() }

    /** 创建 S3Presigner */
    private fun createPresigner(): S3Presigner {
        val credentials = AwsBasicCredentials.create(s3Properties.accessKey, s3Properties.secretKey)
        val s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(s3Properties.pathStyleAccess)
            .build()
        return S3Presigner.builder()
            .endpointOverride(URI.create(s3Properties.endpoint!!))
            .region(Region.of(s3Properties.region ?: "us-east-1"))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(s3Config)
            .build()
    }

    /** 创建带认证的 S3Client（用于下载私有对象） */
    private fun createS3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(s3Properties.accessKey, s3Properties.secretKey)
        val s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(s3Properties.pathStyleAccess)
            .build()
        return S3Client.builder()
            .endpointOverride(URI.create(s3Properties.endpoint!!))
            .region(Region.of(s3Properties.region ?: "us-east-1"))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(s3Config)
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build()
    }

    /**
     * 为单个文件生成预签名 PUT 上传凭证
     * @param fileExtension 文件扩展名，如 "jpg"、"png"（不含点号）
     * @return 预签名上传凭证
     */
    fun generateUploadCredential(fileExtension: String = "bin"): PresignedUploadCredential {
        return runCatching {
            val datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            val timestamp = System.currentTimeMillis()
            val randomStr = (Math.random() * 1_000_000).toLong().toString(36)
            val safeExt = fileExtension.trimStart('.').ifBlank { "bin" }
            val objectKey = "agent/$datePath/${timestamp}_${randomStr}.$safeExt"

            val putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucketName)
                .key(objectKey)
                .build()

            val presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignExpireSeconds))
                .putObjectRequest(putObjectRequest)
                .build()

            val presignedRequest = presigner.presignPutObject(presignRequest)
            val uploadUrl = presignedRequest.url().toString()
            val accessUrl = buildAccessUrl(objectKey)

            PresignedUploadCredential(
                uploadUrl = uploadUrl,
                accessUrl = accessUrl,
                objectKey = objectKey,
                expiration = Date(System.currentTimeMillis() + presignExpireSeconds * 1000),
                maxFileSize = 10485760L
            )
        }.getOrElse { e ->
            throw RuntimeException("生成上传凭证失败", e)
        }
    }

    /**
     * 从对象存储下载图片并转为 base64
     * 使用 S3Client 认证访问，解决私有 bucket 403 问题
     * @param imageUrl 图片访问 URL（如 http://localhost:9000/agentx/agent/xxx.jpg）
     * @return Pair<base64数据, mimeType>
     */
    fun downloadImageAsBase64(imageUrl: String): Pair<String, String> {
        val objectKey = extractObjectKey(imageUrl)
        val request = GetObjectRequest.builder()
            .bucket(s3Properties.bucketName!!)
            .key(objectKey)
            .build()

        val bytes = s3Client.getObject(request, ResponseTransformer.toBytes()).asByteArray()
        val mimeType = detectMimeType(imageUrl)
        val base64Data = Base64.getEncoder().encodeToString(bytes)
        return Pair(base64Data, mimeType)
    }

    /**
     * 从完整图片 URL 中提取 objectKey
     * 支持路径样式：http://endpoint/bucket/key
     * 支持自定义域名：https://custom.domain/key
     */
    private fun extractObjectKey(imageUrl: String): String {
        val endpoint = s3Properties.endpoint!!.trimEnd('/')
        val bucket = s3Properties.bucketName!!

        // 路径样式：endpoint/bucket/key → 提取 key
        val pathStylePrefix = "$endpoint/$bucket/"
        if (imageUrl.startsWith(pathStylePrefix)) {
            return imageUrl.removePrefix(pathStylePrefix)
        }

        // urlPrefix 或 customDomain 配置：prefix/key → 提取 key
        listOfNotNull(s3Properties.urlPrefix, s3Properties.customDomain)
            .map { it.trimEnd('/') + "/" }
            .forEach { prefix ->
                if (imageUrl.startsWith(prefix)) return imageUrl.removePrefix(prefix)
            }

        // 兜底：取最后一段路径作为 key（适配虚拟主机样式）
        return URI.create(imageUrl).path.trimStart('/')
    }

    /** 从 URL 扩展名推断 MIME 类型 */
    private fun detectMimeType(url: String): String {
        return when (url.substringAfterLast('.').lowercase().substringBefore('?')) {
            "jpg", "jpeg" -> "image/jpeg"
            "png"         -> "image/png"
            "gif"         -> "image/gif"
            "webp"        -> "image/webp"
            "bmp"         -> "image/bmp"
            else          -> "image/jpeg"
        }
    }

    /** 构建文件访问 URL */
    private fun buildAccessUrl(objectKey: String): String {
        val endpoint = s3Properties.endpoint!!.trimEnd('/')
        val bucket = s3Properties.bucketName!!
        return when {
            !s3Properties.urlPrefix.isNullOrBlank() ->
                "${s3Properties.urlPrefix!!.trimEnd('/')}/$objectKey"
            !s3Properties.customDomain.isNullOrBlank() ->
                "${s3Properties.customDomain!!.trimEnd('/')}/$objectKey"
            s3Properties.pathStyleAccess ->
                "$endpoint/$bucket/$objectKey"
            else -> {
                val host = endpoint.replace("https://", "").replace("http://", "")
                "https://$bucket.$host/$objectKey"
            }
        }
    }

    // -------------------------------------------------------------------------
    // 数据类
    // -------------------------------------------------------------------------

    data class PresignedUploadCredential(
        val uploadUrl: String,
        val accessUrl: String,
        val objectKey: String,
        val expiration: Date,
        val maxFileSize: Long
    )
}

