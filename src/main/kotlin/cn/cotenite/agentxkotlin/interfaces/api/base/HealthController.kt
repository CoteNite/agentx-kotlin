package cn.cotenite.agentxkotlin.interfaces.api.base

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 20:41
 */
@RestController
@RequestMapping("/health")
class HealthController(
    @Value("\${spring.application.name}")
    private val applicationName:String
){

    @GetMapping
    fun health(): Map<String, Any> {
        val result: MutableMap<String, Any> = HashMap()
        result["status"] = "UP"
        result["service"] = applicationName
        result["timestamp"] = System.currentTimeMillis()
        return result
    }

}
