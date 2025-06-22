package cn.cotenite.agentxkotlin.infrastructure.converter

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:53
 */
@Converter(autoApply = false)
class StringListConverter : AttributeConverter<List<String>?, String?> {

    private val objectMapper: ObjectMapper by lazy { ObjectMapper() }

    override fun convertToDatabaseColumn(attribute: List<String>?): String? {
        if (attribute == null || attribute.isEmpty()) {
            return "[]" // 对于空列表，存储为空JSON数组字符串
        }
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Error converting List<String> to JSON string", e)
        }
    }

    override fun convertToEntityAttribute(dbData: String?): List<String>? {
        if (dbData.isNullOrBlank() || dbData == "[]") {
            return mutableListOf() // 对于空字符串或空JSON数组字符串，返回空的可变列表
        }
        return try {
            // 使用 TypeReference 来确保 Jackson 能够正确反序列化泛型 List<String>
            objectMapper.readValue(dbData, object : TypeReference<List<String>>() {})
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Error converting JSON string to List<String>", e)
        }
    }
}