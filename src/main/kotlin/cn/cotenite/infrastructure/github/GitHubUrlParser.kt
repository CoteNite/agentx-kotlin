package cn.cotenite.infrastructure.github

import cn.cotenite.domain.tool.model.dto.GitHubRepoInfo
import cn.cotenite.infrastructure.exception.BusinessException
import org.slf4j.LoggerFactory

/**
 * @author  yhk
 * Description  
 * Date  2026/4/8 19:23
 */
object GitHubUrlParser {

    private val logger = LoggerFactory.getLogger(GitHubUrlParser::class.java)

    /**
     * GitHub URL 正则表达式
     * group(1): owner
     * group(2): repoName
     * group(3): tree 或 blob (可选)
     * group(4): ref (分支/Tag/Commit SHA)
     * group(5): 仓库内路径 (含起始斜杠)
     * group(6): 纯仓库内路径 (可选)
     */
    private val GITHUB_URL_PATTERN =
        Regex("""^https://github\.com/([\w.-]+)/([\w.-]+)(?:/(tree|blob)/([\w.-]+)(/(.*))?)?$""")

    /**
     * 解析 GitHub URL 的格式。
     * 如果解析成功，返回 GitHubRepoInfo 对象；如果失败，抛出 BusinessException。
     *
     * @param githubUrl 待解析的 GitHub URL
     * @return 包含解析信息的 GitHubRepoInfo 对象
     */
    @JvmStatic
    fun parseGithubUrl(githubUrl: String?): GitHubRepoInfo {
        if (githubUrl.isNullOrBlank()) {
            throw BusinessException("GitHub URL 不能为空。")
        }

        val matchResult = GITHUB_URL_PATTERN.find(githubUrl)
            ?: throw BusinessException("无效的 GitHub URL 格式：$githubUrl")

        // 使用 destructured 语法或 groupValues 获取匹配项
        // groupValues[0] 是整个字符串，[1] 开始是捕获组
        val groups = matchResult.groupValues
        val owner = groups[1]
        val repoName = groups[2]
        val ref = groups[4].takeIf { it.isEmpty() }?:""

        // 这里的逻辑做了简化：直接取 group(6)，它是去掉了前导斜杠的路径
        val pathInRepo = groups[6].takeIf { it.isNotEmpty() }?:""

        logger.info("解析 GitHub URL：owner={}，repo={}，引用（Tag/分支）={}，仓库内路径={}",
            owner, repoName, ref, pathInRepo)

        return GitHubRepoInfo(owner, repoName, ref, pathInRepo)
    }
}