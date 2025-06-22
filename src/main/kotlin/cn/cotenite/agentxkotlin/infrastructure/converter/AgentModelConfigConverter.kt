package cn.cotenite.agentxkotlin.infrastructure.converter

import cn.cotenite.agentxkotlin.domain.agent.model.AgentModelConfig
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:41
 */
@Converter(autoApply = false)
class AgentModelConfigConverter : AttributeConverter<AgentModelConfig?, String?> {

    // 使用 lazy 初始化 ObjectMapper，确保它是单例且线程安全的
    private val objectMapper: ObjectMapper by lazy { ObjectMapper() }

    /**
     * 将 AgentModelConfig 对象转换为数据库列（JSON字符串）
     *
     * @param attribute 要转换的 AgentModelConfig 对象
     * @return 对应的 JSON 字符串
     */
    override fun convertToDatabaseColumn(attribute: AgentModelConfig?): String? {
        if (attribute == null) {
            return null
        }
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: JsonProcessingException) {
            // 根据实际需求处理异常，例如：抛出运行时异常或记录日志
            throw IllegalArgumentException("Error converting AgentModelConfig to JSON string", e)
        }
    }

    /**
     * 将数据库列（JSON字符串）转换为 AgentModelConfig 对象
     *
     * @param dbData 数据库中的 JSON 字符串
     * @return 对应的 AgentModelConfig 对象
     */
    override fun convertToEntityAttribute(dbData: String?): AgentModelConfig? {
        if (dbData.isNullOrBlank()) {
            return null
        }
        return try {
            objectMapper.readValue(dbData, AgentModelConfig::class.java)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Error converting JSON string to AgentModelConfig", e)
        }
    }
}