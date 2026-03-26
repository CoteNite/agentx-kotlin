package cn.cotenite.infrastructure.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * JSON工具类
 */
object JsonUtils {

    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun toJsonString(value: Any?): String = objectMapper.writeValueAsString(value)

    fun <T> parseObject(json: String, clazz: Class<T>): T = objectMapper.readValue(json, clazz)

    fun <T> parseObject(json: String, typeReference: TypeReference<T>): T =
        objectMapper.readValue(json, typeReference)

    fun <T> parseArray(json: String, clazz: Class<T>): List<T> =
        objectMapper.readValue(json, objectMapper.typeFactory.constructCollectionType(List::class.java, clazz))
}
