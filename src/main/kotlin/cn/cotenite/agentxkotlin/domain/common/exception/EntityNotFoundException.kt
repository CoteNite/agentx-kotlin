package cn.cotenite.agentxkotlin.domain.common.exception

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 11:17
 */
class EntityNotFoundException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
