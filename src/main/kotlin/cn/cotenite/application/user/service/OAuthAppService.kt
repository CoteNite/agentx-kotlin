package cn.cotenite.application.user.service

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.client.RestTemplate
import cn.cotenite.domain.user.model.UserEntity
import cn.cotenite.domain.user.service.UserDomainService
import cn.cotenite.infrastructure.config.GitHubOAuthProperties
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.utils.JwtUtils
import cn.cotenite.interfaces.dto.user.GitHubTokenResponse
import cn.cotenite.interfaces.dto.user.GitHubUserInfo
import kotlin.random.Random

/**
 * OAuth 应用服务
 */
@Service
class OAuthAppService(
    restTemplateBuilder: RestTemplateBuilder,
    private val githubProperties: GitHubOAuthProperties,
    private val userDomainService: UserDomainService
) {

    private val restTemplate: RestTemplate = restTemplateBuilder.build()

    fun getGitHubAuthorizeUrl(): String = buildString {
        append(githubProperties.authorizeUrl)
        append("?client_id=${githubProperties.clientId}")
        append("&redirect_uri=${githubProperties.redirectUri}")
        append("&scope=user:email")
    }

    fun handleGitHubCallback(code: String): Map<String, String> = runCatching {
        val tokenResponse = getAccessToken(code)
        val accessToken = tokenResponse.accessToken.takeUnless { it.isNullOrBlank() }
            ?: throw BusinessException("获取GitHub访问令牌失败")

        val userInfo = getUserInfo(accessToken)
        val githubId = userInfo.id ?: throw BusinessException("获取GitHub用户信息失败")
        if (!StringUtils.hasText(userInfo.email)) {
            userInfo.email = getPrimaryEmail(accessToken)
        }

        val user = findOrCreateUser(userInfo.copy(id = githubId))
        mapOf("token" to JwtUtils.generateToken(user.id ?: throw BusinessException("用户不存在")))
    }.getOrElse {
        throw BusinessException("GitHub登录失败: ${it.message}", it)
    }

    private fun getAccessToken(code: String): GitHubTokenResponse {
        val headers = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_JSON)
            contentType = MediaType.APPLICATION_JSON
        }
        val body = mapOf(
            "client_id" to githubProperties.clientId,
            "client_secret" to githubProperties.clientSecret,
            "code" to code,
            "redirect_uri" to githubProperties.redirectUri
        )

        return restTemplate.postForObject(
            githubProperties.tokenUrl,
            HttpEntity(body, headers),
            GitHubTokenResponse::class.java
        ) ?: throw BusinessException("获取GitHub访问令牌失败")
    }

    private fun getUserInfo(accessToken: String): GitHubUserInfo {
        val headers = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_JSON)
            set(HttpHeaders.AUTHORIZATION, "token $accessToken")
        }
        return restTemplate.exchange(
            githubProperties.userInfoUrl!!,
            HttpMethod.GET,
            HttpEntity<Unit>(headers),
            GitHubUserInfo::class.java
        ).body ?: throw BusinessException("获取GitHub用户信息失败")
    }

    private fun getPrimaryEmail(accessToken: String): String? {
        val headers = HttpHeaders().apply {
            accept = listOf(MediaType.APPLICATION_JSON)
            set(HttpHeaders.AUTHORIZATION, "token $accessToken")
        }
        val response = restTemplate.exchange(
            githubProperties.userEmailUrl!!,
            HttpMethod.GET,
            HttpEntity<Unit>(headers),
            List::class.java
        ).body.orEmpty()

        return response
            .filterIsInstance<Map<String, Any?>>()
            .firstOrNull { it["primary"] == true }
            ?.get("email") as? String
    }

    private fun findOrCreateUser(userInfo: GitHubUserInfo): UserEntity {
        val githubId = userInfo.id?.toString().orEmpty()
        val existed = userDomainService.findUserByGithubId(githubId)
            ?: userInfo.email?.takeIf { it.isNotBlank() }?.let(userDomainService::findUserByAccount)

        if (existed != null) {
            existed.githubId = githubId
            existed.githubLogin = userInfo.login
            userInfo.avatarUrl?.takeIf { it.isNotBlank() }?.let { existed.avatarUrl = it }
            userDomainService.updateUserInfo(existed)
            return existed
        }

        val created = userDomainService.register(userInfo.email, null, generateRandomPassword())
        created.githubId = githubId
        created.githubLogin = userInfo.login
        created.avatarUrl = userInfo.avatarUrl
        created.nickname = userInfo.name ?: "github-${userInfo.login.orEmpty()}"
        userDomainService.updateUserInfo(created)
        return created
    }

    private fun generateRandomPassword(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+"
        return (1..16)
            .map { characters[Random.nextInt(characters.length)] }
            .joinToString(separator = "")
    }
}
