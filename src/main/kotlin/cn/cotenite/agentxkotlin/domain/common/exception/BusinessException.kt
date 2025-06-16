package cn.cotenite.agentxkotlin.domain.common.exception

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 11:13
 */
open class BusinessException: RuntimeException {

    private val errorCode: String?

    constructor(message: String) : super(message) {
        this.errorCode = null
    }

    constructor(errorCode: String, message: String) : super(message) {
        this.errorCode = errorCode
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        this.errorCode = null
    }

    constructor(errorCode: String, message: String, cause: Throwable) : super(message, cause) {
        this.errorCode = errorCode
    }



}
