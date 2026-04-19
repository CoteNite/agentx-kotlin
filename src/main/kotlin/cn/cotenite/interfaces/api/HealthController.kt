package cn.cotenite.interfaces.api

import cn.cotenite.interfaces.api.common.Result
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping
@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): Result<Any> = Result.success<Any>().apply { message = "ok" }
}
