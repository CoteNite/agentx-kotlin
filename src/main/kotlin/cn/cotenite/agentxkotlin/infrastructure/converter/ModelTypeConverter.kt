package cn.cotenite.agentxkotlin.infrastructure.converter

import cn.cotenite.agentxkotlin.domain.llm.model.enums.ModelType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:00
 */
@Converter(autoApply = false) // 通常设置为 false，在实体字段上显式指定 @Convert
class ModelTypeConverter : AttributeConverter<ModelType?, String?> {

    override fun convertToDatabaseColumn(attribute: ModelType?): String? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: String?): ModelType? {
        return dbData?.let { ModelType.fromCode(it) }
    }
}