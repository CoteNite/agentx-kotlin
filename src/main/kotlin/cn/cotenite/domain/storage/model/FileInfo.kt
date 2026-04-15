package cn.cotenite.domain.storage.model

import java.time.LocalDateTime

/** 文件信息领域模型 */
data class FileInfo(
    /** 文件唯一标识 */
    var fileId: String? = null,

    /** 原始文件名 */
    var originalName: String? = null,

    /** 存储文件名 */
    var storageName: String? = null,

    /** 文件大小（字节） */
    var fileSize: Long? = null,

    /** 文件类型 */
    var contentType: String? = null,

    /** 存储桶名称 */
    var bucketName: String? = null,

    /** 文件路径 */
    var filePath: String? = null,

    /** 文件访问URL */
    var accessUrl: String? = null,

    /** 文件MD5值 */
    var md5Hash: String? = null,

    /** 创建时间 */
    var createdAt: LocalDateTime = LocalDateTime.now()
)