package cn.cotenite.infrastructure.exception

/**
 * 业务异常类
 */
open class BusinessException : RuntimeException {

    /**
     * 错误码
     */
    var errorCode: String? = null
        private set

    constructor(message: String) : super(message)

    constructor(errorCode: String, message: String) : super(message) {
        this.errorCode = errorCode
    }

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(errorCode: String, message: String, cause: Throwable) : super(message, cause) {
        this.errorCode = errorCode
    }
}
