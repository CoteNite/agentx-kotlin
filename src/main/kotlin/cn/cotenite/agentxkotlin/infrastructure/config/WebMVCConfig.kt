package cn.cotenite.agentxkotlin.infrastructure.config

import cn.cotenite.agentxkotlin.infrastructure.auth.UserAuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:43
 */
@Configuration
class WebMVCConfig(
    val userAuthInterceptor: UserAuthInterceptor
): WebMvcConfigurer{

    override fun addInterceptors(registry: InterceptorRegistry) {
        // 注册用户鉴权拦截器，并指定拦截路径
        registry.addInterceptor(userAuthInterceptor) // 添加拦截路径 - 拦截所有API请求
            .addPathPatterns("/api/**") // 排除不需要鉴权的路径，例如登录、注册等
            .excludePathPatterns("/api/auth/login", "/api/auth/register")
    }


}
