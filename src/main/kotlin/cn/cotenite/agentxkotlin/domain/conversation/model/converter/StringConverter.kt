package cn.cotenite.agentxkotlin.domain.conversation.model.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @Author  RichardYoung
 * @Description String类型的JPA转换器，用于JSONB字段
 * @Date  2025/6/16
 */
@Converter
class StringConverter : AttributeConverter<String?, String?> {
    
    override fun convertToDatabaseColumn(attribute: String?): String? {
        return attribute
    }
    
    override fun convertToEntityAttribute(dbData: String?): String? {
        return dbData
    }
}