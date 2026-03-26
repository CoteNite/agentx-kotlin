package cn.cotenite.infrastructure.exception

/**
 * 参数校验异常类
 */
class ParamValidationException : BusinessException {

    companion object {
        private const val DEFAULT_CODE = "PARAM_VALIDATION_ERROR"
    }

    constructor(message: String) : super(DEFAULT_CODE, message)

    constructor(paramName: String, message: String) : super(
        DEFAULT_CODE,
        "参数[$paramName]无效: $message"
    )
}
