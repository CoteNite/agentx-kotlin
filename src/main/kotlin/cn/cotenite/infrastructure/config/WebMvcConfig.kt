package cn.cotenite.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import cn.cotenite.infrastructure.auth.UserAuthInterceptor

/**
 * Web MVC 配置类
 */
@Configuration
class WebMvcConfig(
    private val userAuthInterceptor: UserAuthInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(userAuthInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/api/auth/login", "/api/auth/register")
    }
}
