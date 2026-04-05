package cn.cotenite.infrastructure.utils

/**
 * 模型响应转 JSON 工具类
 */
object ModelResponseToJsonUtils {

    fun toJson(response: Any?): String = JsonUtils.toJsonString(response)
}
