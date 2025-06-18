package cn.cotenite.agentxkotlin.domain.agent.model.converter

import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import cn.cotenite.agentxkotlin.infrastructure.utils.JsonUtils
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * AgentTool列表的JPA转换器，用于将AgentTool列表转换为JSON字符串存储到数据库，
 * 以及将数据库中的JSON字符串转换回AgentTool列表
 */
@Converter
class AgentToolListConverter : AttributeConverter<MutableList<AgentTool>?, String?> {
    
    override fun convertToDatabaseColumn(attribute: MutableList<AgentTool>?): String? {
        return if (attribute == null || attribute.isEmpty()) null else JsonUtils.toJsonString(attribute)
    }
    
    override fun convertToEntityAttribute(dbData: String?): MutableList<AgentTool>? {
        return if (dbData.isNullOrEmpty()) mutableListOf() else JsonUtils.parseArray(dbData, AgentTool::class.java)
    }
}