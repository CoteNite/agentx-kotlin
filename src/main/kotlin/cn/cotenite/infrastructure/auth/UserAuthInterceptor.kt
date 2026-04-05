package cn.cotenite.infrastructure.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.servlet.HandlerInterceptor
import cn.cotenite.infrastructure.utils.JwtUtils

/**
 * 用户鉴权拦截器
 */
@Component
class UserAuthInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean =
        runCatching {
            val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return false
            }

            val token = authHeader.substring(BEARER_PREFIX.length)
            if (!JwtUtils.validateToken(token)) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return false
            }

            UserContext.setCurrentUserId(JwtUtils.getUserIdFromToken(token))
            true
        }.getOrElse {
            logger.error("用户鉴权失败", it)
            response.status = HttpServletResponse.SC_UNAUTHORIZED
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
