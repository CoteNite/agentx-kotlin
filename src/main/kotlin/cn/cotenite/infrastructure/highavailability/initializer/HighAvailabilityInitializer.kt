package cn.cotenite.infrastructure.highavailability.initializer

import cn.cotenite.domain.llm.service.HighAvailabilityDomainService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * 高可用初始化器
 * 在应用启动时初始化高可用项目和同步模型
 */
@Component
class HighAvailabilityInitializer(
    private val highAvailabilityDomainService: HighAvailabilityDomainService
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(HighAvailabilityInitializer::class.java)

    override fun run(args: ApplicationArguments) {
        logger.info("开始高可用系统初始化...")

        try {
            // 1. 初始化项目
            highAvailabilityDomainService.initializeProject()

            // 2. 批量同步现有模型
            highAvailabilityDomainService.syncAllModelsToGateway()

            logger.info("高可用系统初始化完成")
        } catch (e: Exception) {
            logger.error("高可用系统初始化失败", e)
            // 初始化失败不阻止应用启动
        }
    }
}
