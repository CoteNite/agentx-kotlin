package cn.cotenite.agentxkotlin.infrastructure.converter

import cn.cotenite.agentxkotlin.domain.llm.model.config.LLMModelConfig
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:05
 */
@Converter(autoApply = false) // 通常设置为 false，在实体字段上显式指定 @Convert
class LLMModelConfigConverter : AttributeConverter<LLMModelConfig?, String?> {

    // 使用 lazy 初始化 ObjectMapper，确保它是单例且线程安全的
    private val objectMapper: ObjectMapper by lazy { ObjectMapper() }

    /**
     * 将 LLMModelConfig 对象转换为数据库列（JSON字符串）
     *
     * @param attribute 要转换的 LLMModelConfig 对象
     * @return 对应的 JSON 字符串
     */
    override fun convertToDatabaseColumn(attribute: LLMModelConfig?): String? {
        if (attribute == null) {
            return null // 如果对象为null，存储null
        }
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: JsonProcessingException) {
            // 根据实际需求处理异常，例如：抛出运行时异常或记录日志
            throw IllegalArgumentException("Error converting LLMModelConfig to JSON string", e)
        }
    }

    /**
     * 将数据库列（JSON字符串）转换为 LLMModelConfig 对象
     *
     * @param dbData 数据库中的 JSON 字符串
     * @return 对应的 LLMModelConfig 对象
     */
    override fun convertToEntityAttribute(dbData: String?): LLMModelConfig? {
        if (dbData.isNullOrBlank()) {
            return null // 如果数据库数据为null或空字符串，返回null
        }
        return try {
            objectMapper.readValue(dbData, LLMModelConfig::class.java)
        } catch (e: JsonProcessingException) {
            // 根据实际需求处理异常，例如：抛出运行时异常或记录日志
            throw IllegalArgumentException("Error converting JSON string to LLMModelConfig", e)
        }
    }
}
