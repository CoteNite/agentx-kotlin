package cn.cotenite.infrastructure.initializer

import cn.cotenite.domain.user.model.UserEntity
import cn.cotenite.domain.user.service.UserDomainService
import cn.cotenite.infrastructure.utils.PasswordUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * 默认数据初始化器
 * 在应用启动时自动初始化默认用户数据
 */
@Component
@Order(100) // 确保在其他初始化器之后执行
class DefaultDataInitializer(
    private val userDomainService: UserDomainService
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(DefaultDataInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        log.info("开始初始化AgentX默认数据...")
        try {
            initializeDefaultUsers()
            log.info("AgentX默认数据初始化完成！")
        } catch (e: Exception) {
            log.error("AgentX默认数据初始化失败", e)
            // 不抛出异常，避免影响应用启动
        }
    }

    /** 初始化默认用户 */
    private fun initializeDefaultUsers() {
        log.info("正在初始化默认用户...")
        initializeAdminUser()
        initializeTestUser()
        log.info("默认用户初始化完成")
    }

    /** 初始化管理员用户 */
    private fun initializeAdminUser() {
        val adminEmail = "admin@agentx.ai"
        try {
            val existingAdmin = userDomainService.findUserByAccount(adminEmail)
            if (existingAdmin != null) {
                log.info("管理员用户已存在，跳过初始化: {}", adminEmail)
                return
            }

            val adminUser = UserEntity().apply {
                id = "admin-user-uuid-001"
                nickname = "AgentX管理员"
                email = adminEmail
                phone = ""
                password = PasswordUtils.encode("admin123")
                isAdmin=true
            }

            userDomainService.createDefaultUser(adminUser)
            log.info("管理员用户初始化成功: {} (密码: admin123)", adminEmail)
        } catch (e: Exception) {
            log.error("管理员用户初始化失败: {}", adminEmail, e)
        }
    }

    /** 初始化测试用户 */
    private fun initializeTestUser() {
        val testEmail = "test@agentx.ai"
        try {
            val existingTest = userDomainService.findUserByAccount(testEmail)
            if (existingTest != null) {
                log.info("测试用户已存在，跳过初始化: {}", testEmail)
                return
            }

            val testUser = UserEntity().apply {
                id = "test-user-uuid-001"
                nickname = "测试用户"
                email = testEmail
                phone = ""
                password = PasswordUtils.encode("test123")
            }

            userDomainService.createDefaultUser(testUser)
            log.info("测试用户初始化成功: {} (密码: test123)", testEmail)
        } catch (e: Exception) {
            log.error("测试用户初始化失败: {}", testEmail, e)
        }
    }
}
