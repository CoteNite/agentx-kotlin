package cn.cotenite.domain.llm.service

/**
 * LLM 完成回调接口
 */
interface CompletionCallback {

    fun onPartialResponse(partialResponse: String)

    fun onCompleteResponse(completeResponse: String, inputTokenCount: Int?, outputTokenCount: Int?)

    fun onError(error: Throwable)
}
