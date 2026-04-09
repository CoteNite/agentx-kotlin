package cn.cotenite.infrastructure.config

import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.conversation.constant.MessageType
import cn.cotenite.domain.conversation.constant.Role
import cn.cotenite.domain.llm.model.config.ProviderConfig
import cn.cotenite.domain.llm.model.enums.ModelType
import cn.cotenite.domain.task.constant.TaskStatus
import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.constant.ToolType
import cn.cotenite.domain.tool.constant.UploadType
import cn.cotenite.infrastructure.converter.LLMModelConfigConverter
import cn.cotenite.infrastructure.converter.ListConverter
import cn.cotenite.infrastructure.converter.ListStringConverter
import cn.cotenite.infrastructure.converter.MessageTypeConverter
import cn.cotenite.infrastructure.converter.ModelTypeConverter
import cn.cotenite.infrastructure.converter.ProviderConfigConverter
import cn.cotenite.infrastructure.converter.ProviderProtocolConverter
import cn.cotenite.infrastructure.converter.RoleConverter
import cn.cotenite.infrastructure.converter.TaskStatusConverter
import cn.cotenite.infrastructure.converter.ToolStatusConverter
import cn.cotenite.infrastructure.converter.ToolTypeConverter
import cn.cotenite.infrastructure.converter.UploadTypeConverter
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import jakarta.annotation.PostConstruct
import org.apache.ibatis.session.SqlSessionFactory
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

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
        val typeHandlerRegistry = sqlSessionFactory.configuration.typeHandlerRegistry


        // 确保自动扫描没有生效时，我们手动注册需要的转换器
        typeHandlerRegistry.register(ProviderConfig::class.java, ProviderConfigConverter())
        typeHandlerRegistry.register(List::class.java, ListConverter())
        typeHandlerRegistry.register(List::class.java, ListStringConverter())
        typeHandlerRegistry.register(LLMModelConfig::class.java, LLMModelConfigConverter())
        typeHandlerRegistry.register(ProviderProtocol::class.java, ProviderProtocolConverter())
        typeHandlerRegistry.register(ModelType::class.java, ModelTypeConverter())
        typeHandlerRegistry.register(Role::class.java, RoleConverter())
        typeHandlerRegistry.register(MessageType::class.java, MessageTypeConverter())
        typeHandlerRegistry.register(TaskStatus::class.java, TaskStatusConverter())
        typeHandlerRegistry.register(ToolStatus::class.java, ToolStatusConverter())
        typeHandlerRegistry.register(ToolType::class.java, ToolTypeConverter())
        typeHandlerRegistry.register(UploadType::class.java, UploadTypeConverter())

        logger.info("已手动注册类型处理器，当前数量: {}", typeHandlerRegistry.typeHandlers.size)
    }
}
