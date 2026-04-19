package cn.cotenite.infrastructure.storage

import cn.cotenite.infrastructure.config.S3Properties
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URI
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * S3对象存储服务实现
 * 支持阿里云OSS通过S3协议访问
 */
@Service
@Conditional(S3EnabledCondition::class)
class S3StorageService(private val s3Properties: S3Properties) : StorageService {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val s3Client: S3Client

    init {
        validateConfiguration()
        s3Client = createS3Client()
        logger.info("S3存储服务初始化成功，已连接到端点：{}", s3Properties.endpoint)
    }

    /** 验证配置是否完整 */
    private fun validateConfiguration() {
        require(StringUtils.hasText(s3Properties.accessKey)) { "S3配置错误：access-key 不能为空" }
        require(StringUtils.hasText(s3Properties.secretKey)) { "S3配置错误：secret-key 不能为空" }
        require(StringUtils.hasText(s3Properties.endpoint)) { "S3配置错误：endpoint 不能为空" }
        require(StringUtils.hasText(s3Properties.bucketName)) { "S3配置错误：bucket-name 不能为空" }
    }

    /** 创建S3客户端 */
    private fun createS3Client(): S3Client = runCatching {
        val awsCredentials = AwsBasicCredentials.create(s3Properties.accessKey, s3Properties.secretKey)

        val s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(s3Properties.pathStyleAccess)
            .build()

        S3Client.builder()
            .endpointOverride(URI.create(s3Properties.endpoint!!))
            .region(Region.of(s3Properties.region))
            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
            .serviceConfiguration(s3Config)
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build()
    }.getOrElse { e ->
        logger.error("创建S3客户端失败", e)
        throw RuntimeException("创建S3客户端失败", e)
    }

    override fun isAvailable(): Boolean = true

    override fun uploadFile(file: File, objectKey: String): UploadResult =
        uploadFile(file, objectKey, s3Properties.bucketName!!)

    override fun uploadFile(file: File, objectKey: String, bucketName: String): UploadResult {
        return runCatching {
            val md5Hash = calculateMD5Hex(file)

            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentLength(file.length())
                .contentType(getContentType(file.name))
                .build()

            FileInputStream(file).use { fis ->
                val response = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fis, file.length()))
                val accessUrl = buildAccessUrl(bucketName, objectKey)

                logger.info("文件上传成功: bucket={}, key={}, size={}, etag={}", bucketName, objectKey, file.length(), response.eTag())

                UploadResult(
                    fileId = UUID.randomUUID().toString(),
                    originalName = file.name,
                    storageName = objectKey,
                    fileSize = file.length(),
                    contentType = getContentType(file.name),
                    bucketName = bucketName,
                    filePath = objectKey,
                    accessUrl = accessUrl,
                    md5Hash = md5Hash,
                    etag = response.eTag()
                )
            }
        }.getOrElse { e ->
            logger.error("文件上传失败: bucket={}, key={}", bucketName, objectKey, e)
            throw RuntimeException("文件上传失败", e)
        }
    }

    override fun uploadStream(inputStream: InputStream, objectKey: String, contentLength: Long): UploadResult =
        uploadStream(inputStream, objectKey, contentLength, s3Properties.bucketName!!)

    override fun uploadStream(inputStream: InputStream, objectKey: String, contentLength: Long, bucketName: String): UploadResult {
        return runCatching {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentLength(contentLength)
                .build()

            val response = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength))
            val accessUrl = buildAccessUrl(bucketName, objectKey)

            logger.info("输入流上传成功: bucket={}, key={}, size={}, etag={}", bucketName, objectKey, contentLength, response.eTag())

            UploadResult(
                fileId = UUID.randomUUID().toString(),
                originalName = extractFileName(objectKey),
                storageName = objectKey,
                fileSize = contentLength,
                contentType = getContentType(objectKey),
                bucketName = bucketName,
                filePath = objectKey,
                accessUrl = accessUrl,
                md5Hash = null,
                etag = response.eTag()
            )
        }.getOrElse { e ->
            logger.error("输入流上传失败: bucket={}, key={}", bucketName, objectKey, e)
            throw RuntimeException("输入流上传失败", e)
        }
    }

    override fun deleteFile(objectKey: String): Boolean =
        deleteFile(objectKey, s3Properties.bucketName!!)

    override fun deleteFile(objectKey: String, bucketName: String): Boolean {
        return runCatching {
            val request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build()
            s3Client.deleteObject(request)
            logger.info("文件删除成功: bucket={}, key={}", bucketName, objectKey)
            true
        }.getOrElse { e ->
            logger.error("文件删除失败: bucket={}, key={}", bucketName, objectKey, e)
            false
        }
    }

    override fun fileExists(objectKey: String): Boolean =
        fileExists(objectKey, s3Properties.bucketName!!)

    override fun fileExists(objectKey: String, bucketName: String): Boolean {
        return runCatching {
            val request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build()
            s3Client.headObject(request) != null
        }.getOrElse {
            logger.debug("文件不存在: bucket={}, key={}", bucketName, objectKey)
            false
        }
    }

    override fun generateObjectKey(originalFileName: String, folder: String): String {
        val datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val fileName = "${UUID.randomUUID()}${getFileExtension(originalFileName)}"
        return if (folder.isNotEmpty()) "$folder/$datePath/$fileName" else "$datePath/$fileName"
    }

    /** 构建访问URL */
    private fun buildAccessUrl(bucketName: String, objectKey: String): String {
        val customDomain = s3Properties.customDomain
        val urlPrefix = s3Properties.urlPrefix
        return when {
            !customDomain.isNullOrEmpty() -> "$customDomain/$objectKey"
            !urlPrefix.isNullOrEmpty() -> "$urlPrefix/$objectKey"
            s3Properties.pathStyleAccess -> "${s3Properties.endpoint}/$bucketName/$objectKey"
            else -> {
                val endpoint = s3Properties.endpoint!!.replace("https://", "")
                "https://$bucketName.$endpoint/$objectKey"
            }
        }
    }

    /** 计算文件MD5（十六进制） */
    private fun calculateMD5Hex(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var length: Int
            while (fis.read(buffer).also { length = it } != -1) {
                md.update(buffer, 0, length)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    /** 获取文件扩展名 */
    private fun getFileExtension(fileName: String?): String {
        if (fileName == null || fileName.lastIndexOf('.') == -1) return ""
        return fileName.substring(fileName.lastIndexOf('.'))
    }

    /** 从对象键中提取文件名 */
    private fun extractFileName(objectKey: String?): String {
        if (objectKey == null) return "unknown"
        val lastSlash = objectKey.lastIndexOf('/')
        return if (lastSlash >= 0) objectKey.substring(lastSlash + 1) else objectKey
    }

    /** 根据文件名获取内容类型 */
    private fun getContentType(fileName: String?): String {
        val extension = getFileExtension(fileName).lowercase()
        return when (extension) {
            ".jpg", ".jpeg" -> "image/jpeg"
            ".png" -> "image/png"
            ".gif" -> "image/gif"
            ".pdf" -> "application/pdf"
            ".txt" -> "text/plain"
            ".json" -> "application/json"
            ".xml" -> "application/xml"
            else -> "application/octet-stream"
        }
    }
}
