package cn.cotenite.infrastructure.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory

/** JSON工具类，用于处理JSON转换 */
object JsonUtils {

    private val log = LoggerFactory.getLogger(JsonUtils::class.java)

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        // 注册 Java 8 时间模块
        registerModule(JavaTimeModule())
        // 禁用将日期写为时间戳
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        // 忽略未知属性
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // 只包含非空属性
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    /**
     * 将对象转换为 JSON 字符串
     * @return JSON 字符串，失败返回 "{}"
     */
    fun toJsonString(obj: Any?): String {
        if (obj == null) return "{}"
        return runCatching { objectMapper.writeValueAsString(obj) }
            .onFailure { log.error("JSON序列化失败: {}, 错误: {}", obj::class.simpleName, it.message, it) }
            .getOrDefault("{}")
    }

    /**
     * 将 JSON 字符串转换为指定对象
     * @return 转换后的对象，失败返回 null
     */
    fun <T> parseObject(json: String?, clazz: Class<T>): T? {
        if (json.isNullOrEmpty()) return null
        return runCatching { objectMapper.readValue(json, clazz) }
            .onFailure { log.error("JSON反序列化失败: {}, 错误: {}", clazz.simpleName, it.message, it) }
            .getOrNull()
    }

    /**
     * 将 JSON 字符串转换为指定对象（TypeReference 泛型版本）
     * @return 转换后的对象，失败返回 null
     */
    fun <T> parseObject(json: String?, typeReference: TypeReference<T>): T? {
        if (json.isNullOrEmpty()) return null
        return runCatching { objectMapper.readValue(json, typeReference) }
            .onFailure { log.error("JSON反序列化失败, 错误: {}", it.message, it) }
            .getOrNull()
    }

    /**
     * 将 JSON 字符串转换为 List
     * @return 转换后的 List，失败返回空 List
     */
    fun <T> parseArray(json: String?, clazz: Class<T>): List<T> {
        if (json.isNullOrEmpty()) return emptyList()
        return runCatching {
            val type = objectMapper.typeFactory.constructCollectionType(List::class.java, clazz)
            objectMapper.readValue<List<T>>(json, type)
        }
            .onFailure { log.error("JSON数组反序列化失败: {}", it.message, it) }
            .getOrDefault(emptyList())
    }
}
