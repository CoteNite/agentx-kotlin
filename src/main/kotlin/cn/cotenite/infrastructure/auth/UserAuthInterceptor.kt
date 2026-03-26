package cn.cotenite.infrastructure.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * 用户鉴权拦截器
 */
@Component
class UserAuthInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean =
        runCatching {
            val userId = "1"
            logger.debug("设置用户ID: {}", userId)
            UserContext.setCurrentUserId(userId)
            true
        }.getOrElse {
            logger.error("用户鉴权失败", it)
            false
        }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        UserContext.clear()
    }
}
