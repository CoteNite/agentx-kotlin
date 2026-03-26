package cn.cotenite.interfaces.api.common

/**
 * 通用API响应结果
 */
data class Result<T>(
    var code: Int,
    var message: String,
    var data: T? = null,
    var timestamp: Long = System.currentTimeMillis()
) {

    companion object {
        fun <T> success(): Result<T> = Result(200, "操作成功")

        fun <T> success(data: T): Result<T> = Result(200, "操作成功", data)

        fun <T> success(message: String, data: T): Result<T> = Result(200, message, data)

        fun <T> error(code: Int, message: String): Result<T> = Result(code, message)

        fun <T> serverError(message: String): Result<T> = error(500, message)

        fun <T> badRequest(message: String): Result<T> = error(400, message)

        fun <T> unauthorized(message: String): Result<T> = error(401, message)

        fun <T> forbidden(message: String): Result<T> = error(403, message)

        fun <T> notFound(message: String): Result<T> = error(404, message)
    }
}
