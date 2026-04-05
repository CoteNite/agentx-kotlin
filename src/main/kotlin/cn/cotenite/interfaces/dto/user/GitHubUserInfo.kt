package cn.cotenite.interfaces.dto.user

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * GitHub 用户信息
 */
data class GitHubUserInfo(
    var id: Long? = null,
    var login: String? = null,
    var name: String? = null,
    var email: String? = null,
    @JsonProperty("avatar_url")
    var avatarUrl: String? = null
)
