package cn.cotenite.infrastructure.storage

import java.io.File
import java.io.InputStream

/**
 * 存储服务接口
 */
interface StorageService {

    /** 检查服务是否可用 */
    fun isAvailable(): Boolean

    /**
     * 上传文件
     * @param file 本地文件
     * @param objectKey 对象存储中的文件路径
     * @return 上传结果信息
     */
    fun uploadFile(file: File, objectKey: String): UploadResult

    /**
     * 上传文件到指定桶
     * @param file 本地文件
     * @param objectKey 对象存储中的文件路径
     * @param bucketName 存储桶名称
     * @return 上传结果信息
     */
    fun uploadFile(file: File, objectKey: String, bucketName: String): UploadResult

    /**
     * 上传输入流
     * @param inputStream 输入流
     * @param objectKey 对象存储中的文件路径
     * @param contentLength 内容长度
     * @return 上传结果信息
     */
    fun uploadStream(inputStream: InputStream, objectKey: String, contentLength: Long): UploadResult

    /**
     * 上传输入流到指定桶
     * @param inputStream 输入流
     * @param objectKey 对象存储中的文件路径
     * @param contentLength 内容长度
     * @param bucketName 存储桶名称
     * @return 上传结果信息
     */
    fun uploadStream(inputStream: InputStream, objectKey: String, contentLength: Long, bucketName: String): UploadResult

    /**
     * 删除文件
     * @param objectKey 对象存储中的文件路径
     * @return 是否删除成功
     */
    fun deleteFile(objectKey: String): Boolean

    /**
     * 删除指定桶中的文件
     * @param objectKey 对象存储中的文件路径
     * @param bucketName 存储桶名称
     * @return 是否删除成功
     */
    fun deleteFile(objectKey: String, bucketName: String): Boolean

    /**
     * 检查文件是否存在
     * @param objectKey 对象存储中的文件路径
     * @return 是否存在
     */
    fun fileExists(objectKey: String): Boolean

    /**
     * 检查指定桶中的文件是否存在
     * @param objectKey 对象存储中的文件路径
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    fun fileExists(objectKey: String, bucketName: String): Boolean

    /**
     * 生成对象存储路径
     * @param originalFileName 原始文件名
     * @param folder 文件夹路径
     * @return 生成的对象路径
     */
    fun generateObjectKey(originalFileName: String, folder: String): String
}
