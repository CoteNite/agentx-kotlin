package cn.cotenite.agentxkotlin.infrastructure.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * @Author  JPA配置类
 * @Description JPA相关配置
 * @Date  2025/6/16
 */
@Configuration
@EnableJpaRepositories(
    basePackages = [
        "cn.cotenite.agentxkotlin.domain.conversation.repository",
        "cn.cotenite.agentxkotlin.domain.agent.repository"
    ],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [".*JpaRepository"]
        )
    ]
)
@EnableTransactionManagement
class JpaConfig {
    // JPA已通过Spring Boot自动配置，这里只需启用JPA仓库和事务管理
}