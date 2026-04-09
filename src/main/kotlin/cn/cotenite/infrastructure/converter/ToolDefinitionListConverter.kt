package cn.cotenite.infrastructure.converter

import com.fasterxml.jackson.core.type.TypeReference
import cn.cotenite.domain.tool.model.config.ToolDefinition
import org.apache.ibatis.type.MappedTypes

/** 工具定义列表JSON转换器 */
@MappedTypes(List::class)
class ToolDefinitionListConverter : JsonToStringConverter<List<ToolDefinition>>(
    object : TypeReference<List<ToolDefinition>>() {}
)
