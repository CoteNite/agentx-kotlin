package cn.cotenite.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * GitHub OAuth 配置
 */
@Component
@ConfigurationProperties(prefix = "oauth.github")
class GitHubOAuthProperties {
    var clientId: String? = null
    var clientSecret: String? = null
    var redirectUri: String? = null
    var authorizeUrl: String = "https://github.com/login/oauth/authorize"
    var tokenUrl: String = "https://github.com/login/oauth/access_token"
    var userInfoUrl: String = "https://api.github.com/user"
    var userEmailUrl: String = "https://api.github.com/user/emails"
}
