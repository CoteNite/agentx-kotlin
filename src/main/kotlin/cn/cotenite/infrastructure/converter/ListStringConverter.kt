package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.MappedTypes

/** 字符串列表JSON转换器 */
@Suppress("UNCHECKED_CAST")
@MappedTypes(List::class)
class ListStringConverter : JsonToStringConverter<List<String>>(
    List::class.java as Class<List<String>>
)