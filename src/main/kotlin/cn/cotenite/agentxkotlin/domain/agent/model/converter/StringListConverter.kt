package cn.cotenite.agentxkotlin.domain.agent.model.converter

import cn.cotenite.agentxkotlin.infrastructure.utils.JsonUtils
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * 字符串列表的JPA转换器，用于将字符串列表转换为JSON字符串存储到数据库，
 * 以及将数据库中的JSON字符串转换回字符串列表
 */
@Converter
class StringListConverter : AttributeConverter<MutableList<String>?, String?> {
    
    override fun convertToDatabaseColumn(attribute: MutableList<String>?): String? {
        return if (attribute == null || attribute.isEmpty()) null else JsonUtils.toJsonString(attribute)
    }
    
    override fun convertToEntityAttribute(dbData: String?): MutableList<String>? {
        return if (dbData.isNullOrEmpty()) mutableListOf() else JsonUtils.parseArray(dbData, String::class.java)
    }
}