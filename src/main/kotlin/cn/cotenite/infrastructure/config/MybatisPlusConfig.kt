package cn.cotenite.infrastructure.config

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import org.apache.ibatis.reflection.MetaObject
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

/**
 * MyBatis-Plus配置类
 */
@Configuration
class MybatisPlusConfig : MetaObjectHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun insertFill(metaObject: MetaObject) {
        val now = LocalDateTime.now()
        strictInsertFill(metaObject, "createdAt", LocalDateTime::class.java, now)
        strictInsertFill(metaObject, "updatedAt", LocalDateTime::class.java, now)
        logger.debug("自动填充 createdAt/updatedAt")
    }

    override fun updateFill(metaObject: MetaObject) {
        strictUpdateFill(metaObject, "updatedAt", LocalDateTime::class.java, LocalDateTime.now())
        logger.debug("自动填充 updatedAt")
    }
}
