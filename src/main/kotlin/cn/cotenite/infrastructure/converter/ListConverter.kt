package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.MappedTypes

/**
 * 列表JSON转换器
 */
@MappedTypes(List::class)
class ListConverter : JsonToStringConverter<List<Any>>(List::class.java as Class<List<Any>>)
