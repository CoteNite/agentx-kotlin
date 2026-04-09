package cn.cotenite.domain.tool.service.state.impl

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.service.state.ToolStateProcessor
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.github.GitHubService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name

/** "发布"状态处理器。 负责从源GitHub下载工具内容，并将其发布到目标GitHub仓库。 */
@Service
class PublishingProcessor(
    private val gitHubService: GitHubService
) : ToolStateProcessor {

    private val logger = LoggerFactory.getLogger(PublishingProcessor::class.java)

    override fun getStatus()= ToolStatus.APPROVED

    override fun getNextStatus() = ToolStatus.APPROVED

    override suspend fun process(tool: ToolEntity) {
        var tempDownloadPath: Path? = null
        var tempUnzipPath: Path? = null

        logger.info("工具ID: ${tool.id} 进入 PUBLISHING 状态，开始发布流程。")

        try {
            val sourceGitHubUrl = tool.uploadUrl?.takeIf { it.isNotBlank() }
                ?: throw BusinessException("工具 ${tool.name} 的源 GitHub URL 为空，无法发布。")

            // 1. 解析源仓库信息并获取版本 (Ref/Commit SHA)
            val sourceRepoInfo = gitHubService.resolveSourceRepoInfoWithLatestCommitIfNoRef(sourceGitHubUrl)
            val version = sourceRepoInfo.ref?.takeIf { it.isNotBlank() }
                ?: throw BusinessException("无法确定源 GitHub 仓库的版本号用于发布。")

            // 使用正则清理版本名，确保可以安全作为目录名
            val sanitizedVersion = version.replace(Regex("[^a-zA-Z0-9_.-]"), "_")
            logger.info("将使用源版本 '$version' (清理后为 '$sanitizedVersion') 进行发布。")

            // 2. 下载源仓库归档
            tempDownloadPath = gitHubService.downloadRepositoryArchive(sourceRepoInfo)

            // 3. 创建临时解压目录 (使用 Kotlin 扩展函数简化目录名生成)
            tempUnzipPath = withContext(Dispatchers.IO) {
                Files.createTempDirectory("unzip-${UUID.randomUUID().toString().take(8)}")
            }
            logger.info("源仓库内容将解压到临时目录: $tempUnzipPath")

            // 4. 解压归档 (ZipFile 已实现 AutoCloseable)
            ZipFile(tempDownloadPath.toFile()).use { zipFile ->
                zipFile.extractAll(tempUnzipPath.toString())
            }
            logger.info("源仓库归档文件解压完成。")

            val actualContentRoot = findActualContentRoot(tempUnzipPath, sourceRepoInfo.repoName)
                ?: throw BusinessException("无法在解压的归档中找到实际内容根目录。")

            logger.info("找到实际内容根目录: $actualContentRoot")

            var sourcePathToPublish = actualContentRoot
            val subPath = sourceRepoInfo.pathInRepo
            if (subPath.isNotBlank()) {
                sourcePathToPublish = actualContentRoot.resolve(subPath)
                if (!sourcePathToPublish.exists() || !sourcePathToPublish.isDirectory()) {
                    throw BusinessException("源 URL 中指定的路径 '$subPath' 在下载的内容中不存在或不是目录。")
                }
                logger.info("将从指定子路径发布: $sourcePathToPublish")
            }

            // 5. 定义目标仓库中的路径结构
            val toolIdentifier = "${tool.name}-${sourceRepoInfo.owner}"
            val targetPathInInternalRepo = "$toolIdentifier/$sanitizedVersion"

            // 6. 提交并推送
            val commitMessage = "Publish tool: ${tool.name}, Version: $version (Source: ${sourceRepoInfo.getFullName()}@$version)"
            gitHubService.commitAndPushToTargetRepo(
                sourceDirectoryPath = sourcePathToPublish,
                targetPathInRepo = targetPathInInternalRepo,
                commitMessage =commitMessage
            )

            logger.info("工具 ${tool.name} 版本 $version 成功发布到目标仓库路径 $targetPathInInternalRepo 下")

        } catch (e: Exception) {
            when (e) {
                is BusinessException, is IOException -> {
                    logger.error("发布工具 ${tool.name} (ID: ${tool.id}) 失败: ${e.message}", e)
                    throw BusinessException("发布工具到目标仓库时失败: ${e.message}", e)
                }
                else -> throw e
            }
        } finally {
            cleanupTemporaryFiles(tempDownloadPath, tempUnzipPath)
        }
    }


    /** 清理临时文件和目录 */
    private fun cleanupTemporaryFiles(tempDownloadPath: Path?, tempUnzipPath: Path?) {
        try {
            tempDownloadPath?.takeIf { it.exists() }?.let {
                it.deleteIfExists()
                logger.info("已删除临时下载文件: $it")
            }
            tempUnzipPath?.takeIf { it.exists() }?.let {
                FileUtils.deleteDirectory(it.toFile())
                logger.info("已删除临时解压目录: $it")
            }
        } catch (e: IOException) {
            logger.warn("清理发布过程中的临时文件失败: ${e.message}")
        }
    }

    /** 查找解压后的实际内容根目录 */
    private fun findActualContentRoot(unzipDir: Path, repoNameHint: String?): Path? {
        val subDirs = Files.list(unzipDir).use { stream ->
            stream.filter { it.isDirectory() }.toList()
        }

        if (subDirs.size == 1) {
            logger.info("找到唯一子目录作为内容根: ${subDirs[0]}")
            return subDirs[0]
        }

        if (!repoNameHint.isNullOrBlank()) {
            subDirs.find { it.name.contains(repoNameHint, ignoreCase = true) }?.let {
                logger.info("通过仓库名提示找到内容根: $it")
                return it
            }
        }

        logger.warn("无法精确找到实际内容根目录，将使用解压目录本身。")
        return unzipDir
    }
}