package cn.cotenite.interfaces.dto.user

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * GitHub Token 响应
 */
data class GitHubTokenResponse(
    @JsonProperty("access_token")
    var accessToken: String? = null,
    @JsonProperty("token_type")
    var tokenType: String? = null,
    var scope: String? = null
)
