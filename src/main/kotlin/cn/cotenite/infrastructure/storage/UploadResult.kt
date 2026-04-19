package cn.cotenite.infrastructure.storage

import java.time.LocalDateTime

/**
 * 上传结果数据类
 */
data class UploadResult(
    val fileId: String,
    val originalName: String,
    val storageName: String,
    val fileSize: Long,
    val contentType: String,
    val bucketName: String,
    val filePath: String,
    val accessUrl: String,
    val md5Hash: String?,
    val etag: String?,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
