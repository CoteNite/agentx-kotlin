package cn.cotenite.infrastructure.config

import cn.cotenite.infrastructure.auth.UserAuthInterceptor
import cn.cotenite.infrastructure.interceptor.AdminAuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC 配置类
 */
@Configuration
class WebMvcConfig(
    private val userAuthInterceptor: UserAuthInterceptor,
    private val adminAuthInterceptor: AdminAuthInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(userAuthInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/login", // 登录接口
                "/health", // 健康检查接口
                "/register", // 注册接口
                "/auth/config", // 认证配置接口
                "/send-email-code", "/verify-email-code", "/get-captcha", "/reset-password",
                "/send-reset-password-code", "/oauth/github/authorize", "/oauth/github/callback", "/sso/**", // SSO相关接口
                "/v1/**"
            )

        registry.addInterceptor(adminAuthInterceptor).addPathPatterns("/admin/**");

    }
}
