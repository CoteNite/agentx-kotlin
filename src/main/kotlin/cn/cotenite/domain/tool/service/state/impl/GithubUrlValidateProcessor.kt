package cn.cotenite.domain.tool.service.state.impl

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.model.dto.GitHubRepoInfo
import cn.cotenite.domain.tool.service.state.ToolStateProcessor
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.github.GitHubService
import cn.cotenite.infrastructure.github.GitHubUrlParser
import org.kohsuke.github.GitHubBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

/**
 * @author  yhk
 * Description  
 * Date  2026/4/7 18:52
 */
@Component
class GithubUrlValidateProcessor(
    private val gitHubService: GitHubService
): ToolStateProcessor {

    companion object {
        private val logger = LoggerFactory.getLogger(GithubUrlValidateProcessor::class.java)

        private val GITHUB_URL_PATTERN = Regex(
            "^https://github\\.com/(?<owner>[\\w.-]+)/(?<repo>[\\w.-]+)(?:/(?:tree|blob)/(?<ref>[\\w.-]+)(?<path>/.*)?)?$"
        )

        /**
         * 解析并验证 GitHub URL
         */
        fun parseAndValidateGithubUrl(githubUrl: String?): GitHubRepoInfo {
            if (githubUrl.isNullOrBlank()) {
                throw BusinessException("GitHub URL 不能为空")
            }

            val matchResult = GITHUB_URL_PATTERN.matchEntire(githubUrl)
                ?: throw BusinessException("无效的 GitHub URL 格式: $githubUrl")

            val groups = matchResult.groups as MatchNamedGroupCollection
            val owner = groups["owner"]?.value ?: ""
            val repoName = groups["repo"]?.value ?: ""
            val ref = groups["ref"]?.value
            val pathInRepoRaw = groups["path"]?.value

            // 去掉路径开头的 '/'
            val pathInRepo = pathInRepoRaw?.removePrefix("/")

            logger.info("解析 GitHub URL: owner=$owner, repo=$repoName, ref=$ref, pathInRepo=$pathInRepo")

            return try {
                val github = GitHubBuilder().build()
                val repository = github.getRepository("$owner/$repoName")
                    ?: throw BusinessException("GitHub 仓库不存在: $owner/$repoName")

                if (repository.isPrivate) {
                    throw BusinessException("GitHub 仓库必须是公开的: $owner/$repoName")
                }

                // 验证路径是否存在
                if (!pathInRepo.isNullOrEmpty()) {
                    val effectiveRef = ref ?: repository.defaultBranch
                    try {
                        repository.getFileContent(pathInRepo, effectiveRef)
                        logger.info("路径 $pathInRepo 在 ref $effectiveRef 中验证成功")
                    } catch (_: IOException) {
                        logger.warn("路径验证失败: $pathInRepo 在 $effectiveRef 中不存在")
                        throw BusinessException("GitHub 仓库中指定的路径 '$pathInRepo' 不存在。")
                    }
                }

                logger.info("GitHub URL 验证成功: $githubUrl")

                GitHubRepoInfo(owner, repoName,
                    ref?:throw BusinessException("内部错误"),
                    pathInRepo?:throw BusinessException("内部错误")
                )
            } catch (e: IOException) {
                logger.error("GitHub API 验证失败: $githubUrl", e)
                throw BusinessException("验证 GitHub URL 时发生 API 错误: ${e.message}")
            }
        }
    }

    override suspend fun process(tool: ToolEntity) {
        val uploadUrl = tool.uploadUrl
        // 使用 GitHubUrlParser 进行 URL 的格式解析，允许不带分支/Tag或指向子路径
        val repoInfo= GitHubUrlParser.parseGithubUrl(uploadUrl)
        // 调用 GitHubService 进行 API 层面的仓库、引用（分支/Tag）和路径存在性验证
        try {
            gitHubService.validateGitHubRepoRefAndPath(repoInfo)
            logger.info("GitHub URL 验证成功：{}", uploadUrl)
        } catch (e: IOException) {
            logger.error("通过 GitHubService 验证 URL 失败：{}", uploadUrl, e)
            throw BusinessException("验证 GitHub URL 时发生 API 错误：" + e.message)
        }
    }
    override fun getStatus()= ToolStatus.GITHUB_URL_VALIDATE

    override fun getNextStatus()= ToolStatus.DEPLOYING
}