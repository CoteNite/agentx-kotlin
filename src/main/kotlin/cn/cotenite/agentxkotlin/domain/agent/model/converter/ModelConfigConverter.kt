package cn.cotenite.agentxkotlin.domain.agent.model.converter

import cn.cotenite.agentxkotlin.domain.agent.model.ModelConfig
import cn.cotenite.agentxkotlin.infrastructure.utils.JsonUtils
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * ModelConfig的JPA转换器，用于将ModelConfig对象转换为JSON字符串存储到数据库，
 * 以及将数据库中的JSON字符串转换回ModelConfig对象
 */
@Converter
class ModelConfigConverter : AttributeConverter<ModelConfig?, String?> {
    
    override fun convertToDatabaseColumn(attribute: ModelConfig?): String? {
        return if (attribute == null) null else JsonUtils.toJsonString(attribute)
    }
    
    override fun convertToEntityAttribute(dbData: String?): ModelConfig? {
        return if (dbData.isNullOrEmpty()) null else JsonUtils.parseObject(dbData, ModelConfig::class.java)
    }
}