package cn.cotenite.infrastructure.exception

/**
 * 实体未找到异常
 */
class EntityNotFoundException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
