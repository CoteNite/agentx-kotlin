package cn.cotenite.agentxkotlin.infrastructure.converter

import cn.cotenite.agentxkotlin.domain.conversation.constant.Role
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * Role枚举与数据库字符串之间的转换器
 * @Author RichardYoung
 * @Description JPA属性转换器，用于Role枚举与数据库VARCHAR字段的转换
 * @Date 2025/6/22 01:00
 */
@Converter(autoApply = true)
class RoleConverter : AttributeConverter<Role, String> {

    /**
     * 将Role枚举转换为数据库字符串
     * @param attribute Role枚举值
     * @return 数据库中存储的字符串值
     */
    override fun convertToDatabaseColumn(attribute: Role?): String? {
        return attribute?.name
    }

    /**
     * 将数据库字符串转换为Role枚举
     * @param dbData 数据库中的字符串值
     * @return Role枚举值
     */
    override fun convertToEntityAttribute(dbData: String?): Role? {
        return if (dbData.isNullOrBlank()) {
            null
        } else {
            Role.fromCode(dbData)
        }
    }
}