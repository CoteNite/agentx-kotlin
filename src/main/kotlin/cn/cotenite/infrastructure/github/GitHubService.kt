package cn.cotenite.infrastructure.github

import cn.cotenite.domain.tool.model.dto.GitHubRepoInfo
import cn.cotenite.infrastructure.config.GitHubProperties
import cn.cotenite.infrastructure.exception.BusinessException
import jakarta.annotation.PostConstruct
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PushCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.kohsuke.github.GHRef
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Service
class GitHubService(private val gitHubProperties: GitHubProperties) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GitHubService::class.java)
    }

    private val github: GitHub = GitHubBuilder().build()

    @PostConstruct
    fun init() {
        if (gitHubProperties.target.username.isNullOrBlank()) {
            logger.warn("目标GitHub仓库的用户名未配置 (github.target.username)")
        }
        if (gitHubProperties.target.repoName.isNullOrBlank()) {
            logger.warn("目标GitHub仓库的名称未配置 (github.target.repo-name)")
        }
        if (gitHubProperties.target.token.isNullOrBlank()) {
            logger.warn("目标GitHub仓库的访问令牌未配置 (github.target.token)")
        }
        logger.info(
            "GitHub服务已初始化，目标仓库: {}/{}",
            gitHubProperties.target.username,
            gitHubProperties.target.repoName
        )
    }

    @Throws(IOException::class)
    fun validateGitHubRepoRefAndPath(repoInfo: GitHubRepoInfo) {
        val ref = repoInfo.ref
        val pathInRepo = repoInfo.pathInRepo
        logger.info("开始通过 GitHub API 验证仓库：{}，引用：{}，路径：{}", repoInfo.getFullName(), ref, pathInRepo)

        val repository = github.getRepository(repoInfo.getFullName())
            ?: throw BusinessException("GitHub 仓库不存在：${repoInfo.getFullName()}")
        if (repository.isPrivate) {
            throw BusinessException("GitHub 仓库必须是公开的：${repoInfo.getFullName()}")
        }

        var effectiveRef: String? = null
        if (ref.isBlank()) {
            effectiveRef = repository.defaultBranch
            if (effectiveRef.isNullOrBlank()) {
                throw BusinessException("无法获取仓库 ${repoInfo.getFullName()} 的默认分支。")
            }
            logger.info("URL 未指定引用，默认使用仓库的默认分支：'{}'", effectiveRef)
        } else {
            var refFound = false
            val potentialBranchRef = "heads/$ref"
            val potentialTagRef = "tags/$ref"
            try {
                val branchRef: GHRef? = repository.getRef(potentialBranchRef)
                if (branchRef != null) {
                    effectiveRef = ref
                    refFound = true
                    logger.info("引用 '{}' 已验证为有效的分支。", ref)
                }
            } catch (e: IOException) {
                if (e is FileNotFoundException) {
                    logger.debug("引用 '{}' 不是有效分支 ({})，尝试作为 Tag 验证。", ref, e.message)
                } else {
                    throw e
                }
            }
            if (!refFound) {
                try {
                    val tagRef: GHRef? = repository.getRef(potentialTagRef)
                    if (tagRef != null) {
                        effectiveRef = ref
                        refFound = true
                        logger.info("引用 '{}' 已验证为有效的 Tag。", ref)
                    }
                } catch (e: IOException) {
                    if (e is FileNotFoundException) {
                        logger.debug("引用 '{}' 也不是有效的 Tag ({})。", ref, e.message)
                    } else {
                        throw e
                    }
                }
            }
            if (!refFound) {
                throw BusinessException("指定的引用 '$ref' 在 GitHub 仓库 '${repoInfo.getFullName()}' 中不是一个有效的分支也不是一个有效的 Tag。")
            }
        }

        if (pathInRepo.isNotEmpty()) {
            var apiRefForContent = effectiveRef!!
            try {
                repository.getRef("tags/$effectiveRef")
                apiRefForContent = "tags/$effectiveRef"
            } catch (_: IOException) {
            }

            var pathExists = false
            try {
                repository.getDirectoryContent(pathInRepo, apiRefForContent)
                pathExists = true
                logger.info("路径 '{}' 在引用 '{}' 中验证成功，它是一个目录。", pathInRepo, effectiveRef)
            } catch (e: IOException) {
                if (e !is FileNotFoundException) {
                    throw e
                }
                logger.debug("路径 '{}' 在引用 '{}' 中不是目录，尝试作为文件验证。", pathInRepo, effectiveRef)
            }

            if (!pathExists) {
                try {
                    val fileContent = repository.getFileContent(pathInRepo, apiRefForContent)
                    if (fileContent != null) {
                        pathExists = true
                        logger.info("路径 '{}' 在引用 '{}' 中验证成功，它是一个文件。", pathInRepo, effectiveRef)
                    }
                } catch (e: IOException) {
                    if (e !is FileNotFoundException) {
                        throw e
                    }
                    logger.debug("路径 '{}' 在引用 '{}' 中也不是文件。", pathInRepo, effectiveRef)
                }
            }

            if (!pathExists) {
                throw BusinessException("指定路径 '$pathInRepo' 在 GitHub 仓库的引用 '$effectiveRef' 中不存在或无法访问。")
            }
        }
        logger.info("GitHub 仓库、引用和路径验证通过：{}", repoInfo.getFullName())
    }

    @Throws(IOException::class)
    fun resolveSourceRepoInfoWithLatestCommitIfNoRef(sourceGithubUrl: String): GitHubRepoInfo {
        val basicInfo = GitHubUrlParser.parseGithubUrl(sourceGithubUrl)
        if (basicInfo.ref.isBlank()) {
            logger.info(
                "源URL {} 未指定ref，将获取仓库 {}/{} 的默认分支最新commit SHA",
                sourceGithubUrl,
                basicInfo.owner,
                basicInfo.repoName
            )
            val repository = github.getRepository(basicInfo.getFullName())
            val defaultBranch = repository.defaultBranch
            if (defaultBranch.isNullOrBlank()) {
                throw BusinessException("无法获取仓库 ${basicInfo.getFullName()} 的默认分支。")
            }
            val latestCommitSha = repository.getRef("heads/$defaultBranch").`object`.sha
            logger.info("仓库 {}/{} 的默认分支 {} 最新commit SHA为: {}", basicInfo.owner, basicInfo.repoName, defaultBranch, latestCommitSha)
            return GitHubRepoInfo(basicInfo.owner, basicInfo.repoName, latestCommitSha, basicInfo.pathInRepo)
        }
        return basicInfo
    }

    @Throws(IOException::class)
    fun downloadRepositoryArchive(repoInfo: GitHubRepoInfo): Path {
        logger.info("开始下载仓库归档: {}/{}, ref: {}", repoInfo.owner, repoInfo.repoName, repoInfo.ref)
        val archiveUrlString = "https://github.com/${repoInfo.owner}/${repoInfo.repoName}/zipball/${repoInfo.ref}"
        val tempZipFile = Files.createTempFile("source-repo-${repoInfo.repoName}-${UUID.randomUUID().toString().substring(0, 8)}", ".zip")
        FileUtils.copyURLToFile(URL(archiveUrlString), tempZipFile.toFile(), 30000, 60000)
        logger.info("源仓库归档已下载到: {}", tempZipFile)
        return tempZipFile
    }

    @Throws(IOException::class, GitAPIException::class)
    fun commitAndPushToTargetRepo(sourceDirectoryPath: Path, targetPathInRepo: String, commitMessage: String) {
        val targetUsername = gitHubProperties.target.username
        val targetToken = gitHubProperties.target.token
        val targetRepoName = gitHubProperties.target.repoName
        if (targetUsername.isNullOrBlank()) {
            throw BusinessException("目标GitHub仓库的用户名未配置 (github.target.username)")
        }
        if (targetToken.isNullOrBlank()) {
            throw BusinessException("目标GitHub仓库的Token未配置或为空 (github.target.token)")
        }
        if (targetRepoName.isNullOrBlank()) {
            throw BusinessException("目标GitHub仓库的名称未配置 (github.target.repo-name)")
        }
        doCommitAndPush(sourceDirectoryPath, targetRepoName, targetPathInRepo, commitMessage, targetUsername, targetToken)
    }

    @Throws(IOException::class, GitAPIException::class)
    fun commitAndPushToTargetRepo(sourceDirectoryPath: Path, targetRepoName: String?, targetPathInRepo: String, commitMessage: String) {
        val finalRepoName = if (targetRepoName.isNullOrBlank()) gitHubProperties.target.repoName else targetRepoName
        val targetUsername = gitHubProperties.target.username
        val targetToken = gitHubProperties.target.token
        if (targetUsername.isNullOrBlank()) {
            throw BusinessException("目标GitHub仓库的用户名未配置 (github.target.username)")
        }
        if (targetToken.isNullOrBlank()) {
            throw BusinessException("目标GitHub仓库的Token未配置或为空 (github.target.token)")
        }
        doCommitAndPush(sourceDirectoryPath, finalRepoName, targetPathInRepo, commitMessage, targetUsername, targetToken)
    }

    @Throws(IOException::class, GitAPIException::class)
    private fun doCommitAndPush(
        sourceDirectoryPath: Path,
        targetRepoName: String?,
        targetPathInRepo: String,
        commitMessage: String,
        targetUsername: String,
        targetToken: String
    ) {
        val targetRepoFullName = "$targetUsername/$targetRepoName"
        val targetRemoteUrl = "https://github.com/$targetRepoFullName.git"
        logger.info("准备提交到目标仓库: {}，目标路径: {}，操作用户: {}", targetRemoteUrl, targetPathInRepo, targetUsername)

        val tempCloneDir = Files.createTempDirectory("target-repo-clone-${UUID.randomUUID().toString().substring(0, 8)}")
        var git: Git? = null
        try {
            logger.info("克隆目标仓库 {} 到临时目录 {}", targetRemoteUrl, tempCloneDir)
            git = Git.cloneRepository().setURI(targetRemoteUrl).setDirectory(tempCloneDir.toFile())
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(targetUsername, targetToken)).call()

            val fullTargetPathInClone = tempCloneDir.resolve(targetPathInRepo)
            if (Files.exists(fullTargetPathInClone)) {
                logger.info("目标路径 {} 在克隆仓库中已存在，将被清理。", fullTargetPathInClone)
                FileUtils.deleteDirectory(fullTargetPathInClone.toFile())
            }
            Files.createDirectories(fullTargetPathInClone)
            logger.info("复制文件从源路径 {} 到克隆仓库的目标路径 {}", sourceDirectoryPath, fullTargetPathInClone)
            FileUtils.copyDirectory(sourceDirectoryPath.toFile(), fullTargetPathInClone.toFile())

            logger.info("执行 git add {} (相对于仓库根)", targetPathInRepo)
            git.add().addFilepattern(targetPathInRepo).call()
            logger.info("执行 git commit -m \"{}\"", commitMessage)
            git.commit().setMessage(commitMessage).call()
            logger.info("执行 git push 到远程仓库 {}", targetRemoteUrl)
            val pushCommand: PushCommand = git.push()
            pushCommand.setCredentialsProvider(UsernamePasswordCredentialsProvider(targetUsername, targetToken))
            pushCommand.call()
            logger.info("成功提交并推送到目标GitHub仓库: {}", targetRepoFullName)
        } finally {
            git?.close()
            if (Files.exists(tempCloneDir)) {
                try {
                    FileUtils.deleteDirectory(tempCloneDir.toFile())
                    logger.info("临时克隆目录 {} 已成功清理。", tempCloneDir)
                } catch (e: IOException) {
                    logger.error("清理临时克隆目录 {} 失败: {}", tempCloneDir, e.message)
                }
            }
        }
    }
}
