package cn.cotenite.agentxkotlin.interfaces.api.common

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 20:43
 */
data class Response<T>(
    val code: Int, // 建议将属性改为 val，使其成为不可变数据类
    val message: String?,
    val data: T?,
    val timestamp: Long
) {

    constructor(code: Int, message: String) : this(code, message, null, System.currentTimeMillis())
    constructor(code: Int, message: String, data: T) : this(code, message, data, System.currentTimeMillis())

    companion object {

        fun success(): Response<Unit> {
            return Response(200, "操作成功")
        }


        fun <T> success(data: T): Response<T> {
            return Response(200, "操作成功", data)
        }

        fun <T> success(message: String, data: T): Response<T> {
            return Response(200, message, data)
        }


        fun <T> error(code: Int, message: String?): Response<T> {
            return Response(code, message, null, System.currentTimeMillis())
        }

        /**
         * 服务器内部错误
         *
         * @param message 错误消息
         * @return 响应结果
         */
        fun <T> serverError(message: String): Response<T> {
            return error(500, message)
        }

        /**
         * 参数错误
         *
         * @param message 错误消息
         * @return 响应结果
         */
        fun <T> badRequest(message: String?): Response<T> {
            return error(400, message)
        }

        /**
         * 未授权
         *
         * @param message 错误消息
         * @return 响应结果
         */
        fun <T> unauthorized(message: String): Response<T> {
            return error(401, message)
        }

        /**
         * 禁止访问
         *
         * @param message 错误消息
         * @return 响应结果
         */
        fun <T> forbidden(message: String): Response<T> {
            return error(403, message)
        }

        /**
         * 资源不存在
         *
         * @param message 错误消息
         * @return 响应结果
         */
        fun <T> notFound(message: String?): Response<T> {
            return error(404, message)
        }
    }
}
