package cn.cotenite.infrastructure.auth

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ExternalApiKeyInterceptor: HandlerInterceptor {

    private val logger: Logger = LoggerFactory.getLogger(ExternalApiKeyInterceptor::class.java)


}