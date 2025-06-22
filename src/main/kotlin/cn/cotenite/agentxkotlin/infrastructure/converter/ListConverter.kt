package cn.cotenite.agentxkotlin.infrastructure.converter

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:42
 */
@Converter(autoApply = false)
class ListConverter : AttributeConverter<List<*>?, String?> {

    private val objectMapper: ObjectMapper by lazy { ObjectMapper() }

    /**
     * 将 List 对象转换为数据库列（JSON字符串）
     *
     * @param attribute 要转换的 List 对象
     * @return 对应的 JSON 字符串
     */
    override fun convertToDatabaseColumn(attribute: List<*>?): String? {
        if (attribute == null) {
            return null
        }
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Error converting List to JSON string", e)
        }
    }

    /**
     * 将数据库列（JSON字符串）转换为 List 对象
     *
     * @param dbData 数据库中的 JSON 字符串
     * @return 对应的 List 对象
     */
    override fun convertToEntityAttribute(dbData: String?): List<*>? { // 返回 List<*>
        if (dbData.isNullOrBlank()) {
            return null
        }
        return try {
            objectMapper.readValue(dbData, object : TypeReference<List<*>>() {}) // 尝试通用列表反序列化
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("Error converting JSON string to List", e)
        }
    }
}