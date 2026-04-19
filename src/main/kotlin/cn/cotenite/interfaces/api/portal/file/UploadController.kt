package cn.cotenite.interfaces.api.portal.file

import cn.cotenite.infrastructure.storage.OssUploadService
import cn.cotenite.infrastructure.storage.OssUploadService.PresignedUploadCredential
import cn.cotenite.interfaces.api.common.Result
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 文件上传控制器
 * 返回 S3 预签名 PUT URL，前端使用 PUT 请求直接上传到对象存储
 */
@RestController
@RequestMapping("/upload")
class UploadController(
    private val ossUploadService: OssUploadService
) {

    /**
     * 获取预签名上传凭证
     * @param ext 文件扩展名，如 jpg、png、gif（不含点号，默认 bin）
     * @return 预签名凭证，包含上传 URL 和访问 URL
     */
    @GetMapping("/credential")
    fun getUploadCredential(
        @RequestParam(required = false, defaultValue = "bin") ext: String
    ): Result<PresignedUploadCredential> =
        Result.success(ossUploadService.generateUploadCredential(ext))
}
