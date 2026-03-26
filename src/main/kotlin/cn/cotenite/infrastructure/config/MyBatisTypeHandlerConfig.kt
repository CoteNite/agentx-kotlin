package cn.cotenite.infrastructure.config

import jakarta.annotation.PostConstruct
import org.apache.ibatis.session.SqlSessionFactory
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.conversation.constant.Role
import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.domain.llm.model.enums.ModelType
import cn.cotenite.infrastructure.converter.LLMModelConfigConverter
import cn.cotenite.infrastructure.converter.ListConverter
import cn.cotenite.infrastructure.converter.ModelTypeConverter
import cn.cotenite.infrastructure.converter.ProviderConfigConverter
import cn.cotenite.infrastructure.converter.ProviderProtocolConverter
import cn.cotenite.infrastructure.converter.RoleConverter
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * MyBatis类型处理器配置
 */
@Configuration
class MyBatisTypeHandlerConfig(
    private val sqlSessionFactory: SqlSessionFactory
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun registerTypeHandlers() {
        val registry = sqlSessionFactory.configuration.typeHandlerRegistry

        registry.register(List::class.java, ListConverter())
        registry.register(ModelType::class.java, ModelTypeConverter())
        registry.register(ProviderProtocol::class.java, ProviderProtocolConverter())
        registry.register(Role::class.java, RoleConverter())
        registry.register(ProviderConfig::class.java, ProviderConfigConverter())
        registry.register(LLMModelConfig::class.java, LLMModelConfigConverter())

        logger.info("已手动注册类型处理器，当前数量: {}", registry.typeHandlers.size)
    }
}
