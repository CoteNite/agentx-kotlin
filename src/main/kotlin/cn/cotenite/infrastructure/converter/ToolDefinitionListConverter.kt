package cn.cotenite.infrastructure.converter

import cn.cotenite.domain.tool.model.config.ToolDefinition
import cn.cotenite.infrastructure.utils.JsonUtils
import com.fasterxml.jackson.core.type.TypeReference
import org.apache.ibatis.type.MappedTypes

/** 工具定义列表JSON转换器 */
@MappedTypes(List::class)
class ToolDefinitionListConverter : JsonToStringConverter<List<ToolDefinition>>(
    object : TypeReference<List<ToolDefinition>>() {}
){
    override fun parseJson(json: String?): List<ToolDefinition> {
        if (json==null||json.trim().isEmpty()){
            return emptyList()
        }
        val result = JsonUtils.parseArray(json, ToolDefinition::class.java)
        return result
    }
}
