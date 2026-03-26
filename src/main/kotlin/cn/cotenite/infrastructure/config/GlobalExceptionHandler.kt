package cn.cotenite.infrastructure.config

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.exception.EntityNotFoundException
import cn.cotenite.infrastructure.exception.ParamValidationException
import cn.cotenite.interfaces.api.common.Result
import java.io.IOException

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Configuration
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBusinessException(e: BusinessException, request: HttpServletRequest): Result<Void> {
        logger.error("业务异常: {}, URL: {}", e.message, request.requestURL, e)
        return Result.error(400, e.message ?: "业务处理失败")
    }

    @ExceptionHandler(ParamValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleParamValidationException(e: ParamValidationException, request: HttpServletRequest): Result<Void> {
        logger.error("参数校验异常: {}, URL: {}", e.message, request.requestURL, e)
        return Result.badRequest(e.message ?: "参数校验失败")
    }

    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleEntityNotFoundException(e: EntityNotFoundException, request: HttpServletRequest): Result<Void> {
        logger.error("实体未找到异常: {}, URL: {}", e.message, request.requestURL, e)
        return Result.notFound(e.message ?: "资源不存在")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): Result<Void> {
        val errorMessage = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        logger.error("方法参数校验异常: {}, URL: {}", errorMessage, request.requestURL, e)
        return Result.badRequest(errorMessage)
    }

    @ExceptionHandler(BindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBindException(e: BindException, request: HttpServletRequest): Result<Void> {
        val errorMessage = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        logger.error("表单绑定异常: {}, URL: {}", errorMessage, request.requestURL, e)
        return Result.badRequest(errorMessage)
    }

    @ExceptionHandler(AsyncRequestTimeoutException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun handleAsyncRequestTimeoutException(e: AsyncRequestTimeoutException, request: HttpServletRequest): Any {
        logger.error("异步请求超时: {}", request.requestURL, e)

        return if (request.contentType?.contains(MediaType.TEXT_EVENT_STREAM_VALUE) == true) {
            SseEmitter().apply {
                runCatching {
                    send(SseEmitter.event().name("error").data("{\"message\":\"请求超时，请重试\",\"done\":true}"))
                    complete()
                }.onFailure {
                    if (it is IOException) logger.error("发送SSE超时消息失败", it)
                }
            }
        } else {
            Result.error<Void>(503, "请求处理超时，请重试")
        }
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception, request: HttpServletRequest): Result<Void> {
        logger.error("未预期的异常, URL: {}", request.requestURL, e)
        return Result.serverError("服务器内部错误: ${e.message}")
    }
}
