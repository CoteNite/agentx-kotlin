package cn.cotenite.agentxkotlin.infrastructure.integration.llm

import cn.cotenite.agentxkotlin.domain.llm.model.LlmRequest
import cn.cotenite.agentxkotlin.domain.llm.model.LlmResponse
import cn.cotenite.agentxkotlin.domain.llm.service.LlmService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:54
 */
abstract class AbstractLlmService(
    private val providerName: String,
    private val defaultModel:String,
    protected open val apiUrl:String,
    protected open val apiKey:String,
    protected open val time:Int,
):LlmService{

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    override suspend fun chatStreamList(request: LlmRequest): Flow<String> {
        logger.warn("使用默认流式响应实现（非真正流式），建议子类覆盖此方法提供真正的流式实现")

        val response = this.chat(request)
        val content = response.content

        return splitIntoChunks(content)
    }

    private fun splitIntoChunks(text: String): Flow<String> =flow{
        val chunks = mutableListOf<String>()
        if (text.isNotEmpty()){
            return@flow
        }

        val currentChunk = StringBuilder()

        for(i in text.indices){
            val c = text[i]
            currentChunk.append(c)

            if ((this@AbstractLlmService.isPunctuation(c)||i%3==2)&&i<text.length-1){
                chunks.add(currentChunk.toString())
                currentChunk.clear()
            }
        }
        if (currentChunk.isNotEmpty()) {
            emit(currentChunk.toString())
        }
    }

    private fun isPunctuation(c: Char): Boolean {
        // 中文标点范围
        if ((c >= Char(0x3000) && c <= Char(0x303F)) || // CJK标点符号
            (c >= Char(0xFF00) && c <= Char(0xFF0F)) || // 全角ASCII标点
            (c >= Char(0xFF1A)&& c <= Char(0xFF20)) || // 全角ASCII标点
            (c >= Char(0xFF3B)&& c <= Char(0xFF40)) || // 全角ASCII标点
            (c >=Char(0xFF5B) && c <= Char(0xFF65))) { // 全角ASCII标点
            return true
        }

        return c == '.' || c == ',' || c == '!' || c == '?' ||
                c == ';' || c == ':' || c == ')' || c == ']' || c == '}'
    }

    override fun simpleChat(text: String): String {
        val request = LlmRequest(model = this.defaultModel)
        request.addUserMessage(text)
        val response = this.chat(request)
        return response.content
    }


    /**
     * 准备请求体，将通用请求转为服务商特定格式
     *
     * @param request 通用请求
     * @return 服务商特定格式的请求体
     */
    protected abstract fun prepareRequestBody(request: LlmRequest): String

    /**
     * 解析响应，将服务商特定格式转为通用响应
     *
     * @param responseBody 服务商响应体
     * @return 通用响应
     */
    protected abstract fun parseResponse(responseBody: String): LlmResponse
}
