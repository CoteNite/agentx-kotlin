package cn.cotenite.infrastructure.interceptor

import cn.cotenite.domain.user.service.UserDomainService
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.infrastructure.exception.BusinessException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminAuthInterceptor(
    private val userDomainService: UserDomainService
): HandlerInterceptor{

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val userId = UserContext.getCurrentUserId()
        val user = userDomainService.getUserInfo(userId)
        if (user==null||!user.isAdmin()){
            throw BusinessException("无权限访问管理功能")
        }
        return true
    }
}