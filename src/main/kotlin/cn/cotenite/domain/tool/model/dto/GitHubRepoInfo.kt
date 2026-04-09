package cn.cotenite.domain.tool.model.dto

/**
 * @author  yhk
 * Description  
 * Date  2026/4/7 19:17
 */
data class GitHubRepoInfo(
    val owner: String,
    val repoName: String,
    val ref: String,
    val pathInRepo: String
){


    /** 获取仓库的完整名称，格式为 "owner/repoName"  */
    fun getFullName(): String {
        return owner + "/" + repoName
    }
}