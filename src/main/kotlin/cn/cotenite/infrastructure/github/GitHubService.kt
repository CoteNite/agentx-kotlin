package cn.cotenite.infrastructure.github

import cn.cotenite.domain.tool.model.dto.GitHubRepoInfo
import cn.cotenite.domain.tool.service.state.impl.GithubUrlValidateProcessor
import cn.cotenite.infrastructure.config.GitHubProperties
import cn.cotenite.infrastructure.exception.BusinessException
import org.apache.commons.io.FileUtils
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.kohsuke.github.GitHubBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * 与 GitHub API 交互的服务。
 * 负责从源GitHub仓库下载内容，验证仓库信息，以及将内容推送到目标GitHub仓库。
 */
@Service
class GitHubService(private val gitHubProperties: GitHubProperties) {

    private val logger = LoggerFactory.getLogger(GitHubService::class.java)

    // 初始化 github 实例
    private val github= GitHubBuilder().build()

    @PostConstruct
    fun init() {
        with(gitHubProperties.target) {
            if (username.isNullOrBlank()) logger.warn("目标GitHub仓库的用户名未配置 (github.target.username)")
            if (repoName.isNullOrBlank()) logger.warn("目标GitHub仓库的名称未配置 (github.target.repo-name)")
            if (token.isNullOrBlank()) logger.warn("目标GitHub仓库的访问令牌未配置 (github.target.token)")

            logger.info("GitHub服务已初始化，目标仓库: $username/$repoName")
        }
    }

    /**
     * 验证 GitHub 仓库、引用（分支/Tag）和路径。
     */
    @Throws(IOException::class)
    fun validateGitHubRepoRefAndPath(repoInfo: GitHubRepoInfo) {
        val owner = repoInfo.owner
        val repoName = repoInfo.repoName
        val ref = repoInfo.ref
        val pathInRepo = repoInfo.pathInRepo

        logger.info("开始通过 GitHub API 验证仓库：{}，引用：{}，路径：{}", repoInfo.getFullName(), ref, pathInRepo)

        val repository = github.getRepository(repoInfo.getFullName())
            ?: throw BusinessException("GitHub 仓库不存在：${repoInfo.getFullName()}")

        if (repository.isPrivate) {
            throw BusinessException("GitHub 仓库必须是公开的：${repoInfo.getFullName()}")
        }

        // 决定有效的 Ref
        val effectiveRef = if (ref.isNullOrBlank()) {
            repository.defaultBranch?.also {
                logger.info("URL 未指定引用，默认使用仓库的默认分支：'$it'")
            } ?: throw BusinessException("无法获取仓库 ${repoInfo.getFullName()} 的默认分支。")
        } else {
            validateRef(repository, ref)
        }

        // 验证路径
        if (!pathInRepo.isNullOrBlank()) {
            validatePath(repository, effectiveRef, pathInRepo)
        }

        logger.info("GitHub 仓库、引用和路径验证通过：{}", repoInfo.getFullName())
    }

    private fun validateRef(repository: org.kohsuke.github.GHRepository, ref: String): String {
        // 尝试作为分支 (heads/) 或 Tag (tags/) 验证
        for (prefix in listOf("heads/", "tags/")) {
            try {
                if (repository.getRef("$prefix$ref") != null) {
                    logger.info("引用 '$ref' 已验证为有效的 ${prefix.removeSuffix("/")}。")
                    return ref
                }
            } catch (e: FileNotFoundException) {
                logger.debug("引用 '$ref' 不是有效的 $prefix")
            }
        }
        throw BusinessException("指定的引用 '$ref' 在仓库 '${repository.getFullName()}' 中不是有效的分支或 Tag。")
    }

    private fun validatePath(repository: org.kohsuke.github.GHRepository, ref: String, path: String) {
        // 尝试确定是 Tag 还是分支以适配 API
        val apiRef = try {
            repository.getRef("tags/$ref")
            "tags/$ref"
        } catch (e: IOException) { ref }

        val exists = try {
            // 1. 尝试作为目录
            repository.getDirectoryContent(path, apiRef)
            logger.info("路径 '$path' 验证成功，是一个目录。")
            true
        } catch (e: FileNotFoundException) {
            try {
                // 2. 尝试作为文件
                repository.getFileContent(path, apiRef) != null
            } catch (e2: FileNotFoundException) {
                false
            }
        }

        if (!exists) {
            throw BusinessException("指定路径 '$path' 在引用 '$ref' 中不存在。")
        }
    }

    /**
     * 解析源GitHub URL。如果未指定ref，则获取默认分支最新commit SHA。
     */
    @Throws(IOException::class)
    fun resolveSourceRepoInfoWithLatestCommitIfNoRef(sourceGithubUrl: String): GitHubRepoInfo {
        val basicInfo = GitHubUrlParser.parseGithubUrl(sourceGithubUrl)

        if (basicInfo.ref.isBlank()) {
            val repository = github.getRepository(basicInfo.getFullName())
            val defaultBranch = repository.defaultBranch
                ?: throw BusinessException("无法获取仓库 ${basicInfo.getFullName()} 的默认分支。")

            val latestCommitSha = repository.getRef("heads/$defaultBranch").`object`.sha
            return GitHubRepoInfo(basicInfo.owner, basicInfo.repoName, latestCommitSha, basicInfo.pathInRepo)
        }
        return basicInfo
    }

    /**
     * 下载 ZIP 归档
     */
    @Throws(IOException::class)
    fun downloadRepositoryArchive(repoInfo: GitHubRepoInfo): Path {
        val archiveUrl = "https://github.com/${repoInfo.owner}/${repoInfo.repoName}/zipball/${repoInfo.ref}"
        val tempZipFile = Files.createTempFile("source-repo-${repoInfo.repoName}-${UUID.randomUUID().toString().take(8)}", ".zip")

        FileUtils.copyURLToFile(URL(archiveUrl), tempZipFile.toFile(), 30000, 60000)
        return tempZipFile
    }

    /**
     * 提交并推送到目标仓库
     */
    @Throws(IOException::class, org.eclipse.jgit.api.errors.GitAPIException::class)
    fun commitAndPushToTargetRepo(
        sourceDirectoryPath: Path,
        targetRepoName: String? = null,
        targetPathInRepo: String,
        commitMessage: String
    ) {
        val finalRepoName = targetRepoName ?: gitHubProperties.target.repoName
        val (username, token) = gitHubProperties.target.let { it.username to it.token }

        if (username.isNullOrBlank() || token.isNullOrBlank() || finalRepoName.isNullOrBlank()) {
            throw BusinessException("目标仓库配置不完整。")
        }

        val remoteUrl = "https://github.com/$username/$finalRepoName.git"
        val tempCloneDir = Files.createTempDirectory("target-repo-clone-${UUID.randomUUID().toString().take(8)}")

        try {
            Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(tempCloneDir.toFile())
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(username, token))
                .call().use { git ->
                    val fullTargetPath = tempCloneDir.resolve(targetPathInRepo)
                    if (Files.exists(fullTargetPath)) {
                        FileUtils.deleteDirectory(fullTargetPath.toFile())
                    }
                    Files.createDirectories(fullTargetPath)

                    FileUtils.copyDirectory(sourceDirectoryPath.toFile(), fullTargetPath.toFile())

                    git.add().addFilepattern(targetPathInRepo).call()
                    git.commit().setMessage(commitMessage).call()

                    git.push()
                        .setCredentialsProvider(UsernamePasswordCredentialsProvider(username, token))
                        .call()
                }
        } finally {
            FileUtils.deleteQuietly(tempCloneDir.toFile())
        }
    }
}