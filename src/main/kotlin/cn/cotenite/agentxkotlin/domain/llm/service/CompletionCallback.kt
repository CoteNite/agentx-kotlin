package cn.cotenite.agentxkotlin.domain.llm.service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 00:50
 */
interface CompletionCallback {

    /**
     * 处理部分响应
     *
     * @param partialResponse 部分响应内容
     */
    fun onPartialResponse(partialResponse: String)

    /**
     * 处理完整响应
     *
     * @param completeResponse 完整响应内容
     * @param inputTokenCount  输入token数量
     * @param outputTokenCount 输出token数量
     */
    fun onCompleteResponse(completeResponse: String, inputTokenCount: Int, outputTokenCount: Int)

    /**
     * 处理错误
     *
     * @param error 错误信息
     */
    fun onError(error: Throwable)

}