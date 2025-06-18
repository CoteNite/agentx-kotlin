package cn.cotenite.agentxkotlin.domain.llm.service

import cn.cotenite.agentxkotlin.domain.llm.model.LlmRequest
import cn.cotenite.agentxkotlin.domain.llm.model.LlmResponse
import kotlinx.coroutines.flow.Flow

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 00:51
 */
interface LlmService{

    /**
     * 发送请求到LLM服务商
     *
     * @param request LLM请求
     * @return LLM响应
     */
    fun chat(request: LlmRequest): LlmResponse

    /**
     * 发送流式请求到LLM服务商，返回文本块列表
     *
     * @param request LLM请求
     * @return 文本块列表
     */
    suspend fun chatStreamList(request: LlmRequest): Flow<String>

    /**
     * 发送简单的文本请求
     *
     * @param text 用户输入文本
     * @return 生成的响应内容
     */
    fun simpleChat(text: String): String

    /**
     * 获取服务商名称
     *
     * @return 服务商名称
     */
    fun getProviderName(): String

    /**
     * 获取默认模型名称
     *
     * @return 默认模型名称
     */
    fun getDefaultModel(): String

}
