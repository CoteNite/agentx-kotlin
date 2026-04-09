package cn.cotenite.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * GitHub 配置属性类
 * 用于集中管理所有 GitHub 相关的配置参数
 */
@Configuration
@ConfigurationProperties(prefix = "github")
class GitHubProperties {

    // 默认实例化内部配置类
    var target: Target = Target()

    /**
     * 目标仓库配置
     */
    class Target {
        var username: String? = null // 目标仓库的用户名/组织名
        var repoName: String? = null // 目标仓库名称
        var token: String? = null    // 访问令牌
    }
}