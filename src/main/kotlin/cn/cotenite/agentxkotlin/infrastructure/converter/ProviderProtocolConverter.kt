package cn.cotenite.agentxkotlin.infrastructure.converter

import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:08
 */
@Converter(autoApply = false) // 通常设置为 false，在实体字段上显式指定 @Convert
class ProviderProtocolConverter : AttributeConverter<ProviderProtocol?, String?> { // 将 Int 更改为 String

    override fun convertToDatabaseColumn(attribute: ProviderProtocol?): String? {
        return attribute?.name // 将枚举的 name（字符串）存储到数据库
    }

    override fun convertToEntityAttribute(dbData: String?): ProviderProtocol? {
        // 使用枚举类中提供的 fromCode 方法来转换
        return dbData?.let { ProviderProtocol.fromCode(it) }
    }
}