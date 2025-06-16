package cn.cotenite.agentxkotlin.infrastructure.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:42
 */
@Configuration
class WebConfig {

    @Bean
    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
        val config = CorsConfiguration()
        // 允许所有来源
        config.addAllowedOriginPattern("*")
        // 允许携带认证信息
        config.allowCredentials = true
        // 允许所有请求方法
        config.addAllowedMethod("*")
        // 允许所有请求头
        config.addAllowedHeader("*")
        // 预检请求有效期(秒)
        config.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        val bean: FilterRegistrationBean<CorsFilter> = FilterRegistrationBean<CorsFilter>(CorsFilter(source))
        // 设置过滤器优先级最高
        bean.order = Ordered.HIGHEST_PRECEDENCE
        return bean
    }

}
