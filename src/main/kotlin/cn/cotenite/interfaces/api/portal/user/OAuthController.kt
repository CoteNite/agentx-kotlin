package cn.cotenite.interfaces.api.portal.user

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.application.user.service.OAuthAppService
import cn.cotenite.interfaces.api.common.Result

/**
 * OAuth 控制器
 */
@RestController
@RequestMapping("/oauth")
class OAuthController(
    private val oauthAppService: OAuthAppService
) {

    @GetMapping("/github/authorize")
    fun authorizeGitHub(): Result<Map<String, String>> =
        Result.success(mapOf("authorizeUrl" to oauthAppService.getGitHubAuthorizeUrl()))

    @GetMapping("/github/callback")
    fun githubCallback(@RequestParam code: String): Result<Map<String, String>> =
        Result.success("GitHub登录成功", oauthAppService.handleGitHubCallback(code))
}
