package cn.cotenite.agentxkotlin.infrastructure.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:35
 */
@Component
class UserAuthInterceptor:HandlerInterceptor{

    companion object{
        private val logger= LoggerFactory.getLogger(UserAuthInterceptor::class.java)
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        try {
            // 验证用户身份（正常情况下应该从Token中解析用户ID）
            // 但根据需求，这里直接mock一个用户ID为1
            val userId = "1"
            logger.debug("设置用户ID: {}", userId)

            // 将用户ID设置到上下文
            UserContext.setCurrentUserId(userId)
            return true
        } catch (e: Exception) {
            logger.error("用户鉴权失败", e)
            return false
        }
    }

    override fun afterCompletion(
        request: HttpServletRequest, response: HttpServletResponse, handler: Any,
        ex: Exception?
    ) {
        UserContext.clear()
    }
}
