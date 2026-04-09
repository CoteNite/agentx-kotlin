package cn.cotenite.infrastructure.converter

import org.apache.ibatis.type.MappedTypes

/** Map对象JSON转换器 */
@Suppress("UNCHECKED_CAST")
@MappedTypes(Map::class)
class MapConverter : JsonToStringConverter<Map<String, Any>>(
    Map::class.java as Class<Map<String, Any>>
)