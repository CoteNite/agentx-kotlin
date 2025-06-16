package cn.cotenite.agentxkotlin.infrastructure.config

import cn.cotenite.agentxkotlin.domain.common.exception.BusinessException
import cn.cotenite.agentxkotlin.domain.common.exception.EntityNotFoundException
import cn.cotenite.agentxkotlin.domain.common.exception.ParamValidationException
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

import java.util.stream.Collectors

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:37
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    companion object{
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBusinessException(e: BusinessException, request: HttpServletRequest): Response<Void> {
        logger.error("业务异常: ${e.message}, URL: ${request.requestURL}", e)
        return Response.error(400, e.message)
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(ParamValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleParamValidationException(e: ParamValidationException, request: HttpServletRequest): Response<Void> {
        logger.error("参数校验异常: ${e.message}, URL: ${request.requestURL}", e)
        return Response.badRequest(e.message)
    }

    /**
     * 处理实体未找到异常
     */
    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleEntityNotFoundException(e: EntityNotFoundException, request: HttpServletRequest): Response<Void> {
        logger.error("实体未找到异常: ${e.message}, URL: ${request.requestURL}", e)
        return Response.notFound(e.message)
    }

    /**
     * 处理方法参数校验异常（@Valid注解导致的异常）
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): Response<Void> {
        val fieldErrors = e.bindingResult.fieldErrors
        val errorMessage = fieldErrors.stream()
            .map { error: FieldError -> error.field + ": " + error.defaultMessage }
            .collect(Collectors.joining(", "))

        logger.error("方法参数校验异常: ${errorMessage}, URL: ${request.requestURL}", e)
        return Response.badRequest(errorMessage)
    }

    /**
     * 处理表单绑定异常
     */
    @ExceptionHandler(BindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBindException(e: BindException, request: HttpServletRequest): Response<Void> {
        val fieldErrors = e.bindingResult.fieldErrors
        val errorMessage = fieldErrors.stream()
            .map { error: FieldError -> error.field + ": " + error.defaultMessage }
            .collect(Collectors.joining(", "))

        logger.error("表单绑定异常: ${errorMessage}, URL: ${request.requestURL}", e)
        return Response.badRequest(errorMessage)
    }

    /**
     * 处理未预期的异常
     */
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception, request: HttpServletRequest?): Response<Void> {
        logger.error("未预期的异常: ", e)
        return Response.serverError("服务器内部错误: ${e.message}")
    }

}
