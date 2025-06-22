package cn.cotenite.agentxkotlin.infrastructure.transport

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:24
 */
interface MessageTransport<T> {

    /**
     * 创建连接
     * @param timeout 超时时间(毫秒)
     * @return 连接对象
     */
    fun createConnection(timeout: Long): T

    /**
     * 发送消息
     * @param connection 连接对象
     * @param content 消息内容
     * @param isDone 是否完成
     * @param provider 服务商名称
     * @param model 模型名称
     */
    fun sendMessage(connection: T, content: String, isDone: Boolean, provider: String, model: String)

    /**
     * 完成连接
     * @param connection 连接对象
     */
    fun completeConnection(connection: T)

    /**
     * 处理错误
     * @param connection 连接对象
     * @param error 错误对象
     */
    fun handleError(connection: T, error: Throwable)

}