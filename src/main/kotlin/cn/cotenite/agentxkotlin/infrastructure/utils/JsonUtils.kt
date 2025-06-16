package cn.cotenite.agentxkotlin.infrastructure.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 03:09
 */
object JsonUtils {

    private val log=LoggerFactory.getLogger(JsonUtils::class.java)
    private val objectMapper:ObjectMapper= ObjectMapper()

    init {
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    fun toJsonString(obj: Any?):String{
        if (obj==null){ return "{}" }

        try {
            return objectMapper.writeValueAsString(obj)
        }catch (e:Exception){
            log.error("Json序列化失败：${obj.javaClass.simpleName}，错误：${e.message}",e)
            return "{}"
        }
    }

    fun <T> parseObject(json:String?,clazz: Class<T>): T? {
        if (json.isNullOrEmpty()){
            return null
        }
        try {
            return objectMapper.readValue(json, clazz)
        }catch (e:Exception){
            log.error("Json反序列化失败：${clazz.simpleName}，错误：${e.message}",e)
            return null
        }
    }

    fun <T> parseArray(json:String?,clazz: Class<T>): MutableList<T> {
        if (json.isNullOrEmpty()){ return Collections.emptyList() }

        try {
            val type = objectMapper.typeFactory.constructCollectionType(MutableList::class.java, clazz)
            return objectMapper.readValue(json, type)
        }catch (e:Exception){
            log.error("Json反序列化失败：${clazz.simpleName}，错误：${e.message}",e)
            return Collections.emptyList()
        }
    }

}
