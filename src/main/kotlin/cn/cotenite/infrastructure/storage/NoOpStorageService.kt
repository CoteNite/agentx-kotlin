package cn.cotenite.infrastructure.storage

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream

/**
 * 无操作存储服务实现
 * 当S3配置不完整时使用，提供友好的错误提示
 */
@Service
@Conditional(S3DisabledCondition::class)
class NoOpStorageService : StorageService {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val ERROR_MESSAGE =
            "S3存储服务未配置或配置不完整，无法使用存储功能。请配置以下参数：s3.access-key, s3.secret-key, s3.endpoint, s3.bucket-name"
    }

    init {
        logger.warn("S3存储服务配置不完整，存储相关功能将不可用。请检查以下配置项：")
        logger.warn("  - s3.access-key")
        logger.warn("  - s3.secret-key")
        logger.warn("  - s3.endpoint")
        logger.warn("  - s3.bucket-name")
        logger.warn("如需使用存储功能，请在application.yml中正确配置S3相关参数")
    }

    override fun isAvailable(): Boolean = false

    override fun uploadFile(file: File, objectKey: String): UploadResult =
        throw UnsupportedOperationException(ERROR_MESSAGE)

    override fun uploadFile(file: File, objectKey: String, bucketName: String): UploadResult =
        throw UnsupportedOperationException(ERROR_MESSAGE)

    override fun uploadStream(inputStream: InputStream, objectKey: String, contentLength: Long): UploadResult =
        throw UnsupportedOperationException(ERROR_MESSAGE)

    override fun uploadStream(inputStream: InputStream, objectKey: String, contentLength: Long, bucketName: String): UploadResult =
        throw UnsupportedOperationException(ERROR_MESSAGE)

    override fun deleteFile(objectKey: String): Boolean =
        throw UnsupportedOperationException(ERROR_MESSAGE)

    override fun deleteFile(objectKey: String, bucketName: String): Boolean =
        throw UnsupportedOperationException(ERROR_MESSAGE)

    override fun fileExists(objectKey: String): Boolean =
        throw UnsupportedOperationException(ERROR_MESSAGE)

    override fun fileExists(objectKey: String, bucketName: String): Boolean =
        throw UnsupportedOperationException(ERROR_MESSAGE)

    override fun generateObjectKey(originalFileName: String, folder: String): String =
        throw UnsupportedOperationException(ERROR_MESSAGE)
}
