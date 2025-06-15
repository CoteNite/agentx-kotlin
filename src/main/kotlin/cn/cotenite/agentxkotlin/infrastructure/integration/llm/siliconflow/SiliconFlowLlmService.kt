package cn.cotenite.agentxkotlin.infrastructure.integration.llm.siliconflow

import cn.cotenite.agentxkotlin.domain.llm.model.LlmRequest
import cn.cotenite.agentxkotlin.domain.llm.model.LlmResponse
import cn.cotenite.agentxkotlin.infrastructure.integration.llm.AbstractLlmService
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.servlet.view.BeanNameViewResolver

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 18:25
 */
@Service
class SiliconFlowLlmService(
    @Value("\${llm.provider.providers.siliconflow.name:SiliconFlow}")
    private val providerName: String,
    @Value("\${llm.provider.providers.siliconflow.model:llama3}")
    private val defaultModel: String,
    @Value("\${llm.provider.providers.siliconflow.api-url:https://api.siliconflow.cn/v1/chat/completions}")
    override var apiUrl: String,
    @Value("\${llm.provider.providers.siliconflow.api-key:}")
    final override var apiKey: String,
    @Value("\${llm.provider.providers.siliconflow.timeout:30000}")
    override var time: Int,
    private val beanNameViewResolver: BeanNameViewResolver,
): AbstractLlmService(providerName, apiUrl, apiKey, defaultModel, time) {

    init {
        if (this.apiKey.isEmpty()) {
            logger.warn("SiliconFlow API密钥未配置，请通过环境变量SILICONFLOW_API_KEY设置")
        } else {
            logger.info("初始化SiliconFlow服务，默认模型: {}", defaultModel)
        }
    }

    override fun chat(request: LlmRequest): LlmResponse {
        if ("default" == request.model) {
            logger.info("未指定模型或使用默认模型，使用配置的默认模型: $defaultModel")
            request.model = defaultModel
        }

        try {
            logger.info("发送请求到SiliconFlow服务, 模型: ${request.model}, 消息数: ${request.messages.size}")

            val requestBody = this.prepareRequestBody(request)
            val responseBody = this.sendHttpRequest(requestBody)

            logger.debug("SiliconFlow服务的响应长度为${responseBody.length}")

            return this.parseResponse(responseBody)
        } catch (e: Exception) {
            logger.error("调用SiliconFlow服务出错", e)
            val errorResponse = LlmResponse(
                content = "调用服务时发生错误${e.message}",
                provider = providerName,
                model = request.model
            )
            return errorResponse
        }
    }

    override fun getProviderName(): String {
        return this.providerName
    }

    override fun getDefaultModel(): String {
        return this.defaultModel
    }

    private fun sendHttpRequest(requestBody: String): String {
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(time)
            .setSocketTimeout(time)
            .build()

        val httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build()

        // Fixed: Use apiUrl instead of apiKey for the HttpPost URL
        val httpPost = HttpPost(apiUrl)
        httpPost.setHeader("Content-Type", "application/json")
        httpPost.setHeader("Authorization", "Bearer $apiKey")

        val entity = StringEntity(requestBody, ContentType.APPLICATION_JSON)
        httpPost.entity = entity

        logger.debug("发送http请求到$apiUrl")

        val responseBodyString = httpClient.execute(httpPost).use { response ->
            val statusCode = response.statusLine.statusCode
            logger.debug("HTTP响应的状态码为$statusCode")
            if (statusCode != 200) {
                logger.error("HTTP响应失败，状态码为$statusCode")
            }
            val responseBody = response.entity
            EntityUtils.toString(responseBody)
        }

        return responseBodyString
    }

    override fun prepareRequestBody(request: LlmRequest): String {
        val requestJson = JSONObject()
        requestJson["model"] = request.model

        val messagesJson = JSONArray()

        request.messages.forEach { message ->
            val messageJson = JSONObject()
            messageJson["role"] = message.role
            messageJson["content"] = message.content
            messagesJson.add(messageJson)
        }

        requestJson["messages"] = messagesJson
        requestJson["temperature"] = request.temperature
        requestJson["max_tokens"] = request.maxTokens
        requestJson["stream"] = request.stream

        return requestJson.toJSONString()
    }

    override fun parseResponse(responseBody: String): LlmResponse {
        val responseJson = JSON.parseObject(responseBody)

        val response = LlmResponse(
            provider = providerName,
            model = defaultModel,
        )

        try {
            val choices = responseJson.getJSONArray("choices").getJSONObject(0)
            val message = choices.getJSONObject("message")
            val content = message.getString("content")

            response.content = content
            response.finishReason = choices.getString("finish_reason")

            if (responseJson.containsKey("usage")) {
                val usage = responseJson.getJSONObject("usage")
                response.tokenUsage = usage.getIntValue("total_tokens")
                logger.info("消耗的token数为${response.tokenUsage}")
            }

            return response
        } catch (e: Exception) {
            logger.error("解析SiliconFlow服务返回的响应出错")
            response.content = "解析服务器响应出错${e.message}"
            return response
        }
    }

    @FunctionalInterface
    interface StreamResponseHandler {
        fun onChunk(chunk: String, isLast: Boolean)
    }

    fun streamChat(request: LlmRequest, handler: StreamResponseHandler) {
        if (request.model == "default") {
            logger.info("未指定模型或使用默认模型，使用配置的默认模型: $defaultModel")
            request.model = defaultModel
        }

        try {
            logger.info("发送请求到SiliconFlow服务, 模型: ${request.model}, 消息数: ${request.messages.size}")

            request.stream = true

            val requestBody = this.prepareRequestBody(request)

            this.sendStreamHttpRequest(requestBody, handler)
        } catch (e: Exception) {
            logger.error("调用SiliconFlow服务出错", e)
            handler.onChunk("调用SiliconFlow服务出错${e.message}", true)
        }
    }

    fun streamChat(request: LlmRequest, onChunk: (String, Boolean) -> Unit) {
        this.streamChat(request, object : StreamResponseHandler {
            override fun onChunk(chunk: String, isLast: Boolean) {
                onChunk(chunk, isLast)
            }
        })
    }

    private fun sendStreamHttpRequest(requestBody: String, handler: StreamResponseHandler) {
        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(time)
            .setSocketTimeout(time)
            .build()

        val httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build()

        // Fixed: Use apiUrl instead of apiKey for the HttpPost URL
        val httpPost = HttpPost(apiUrl)
        httpPost.setHeader("Content-Type", "application/json")
        httpPost.setHeader("Authorization", "Bearer $apiKey")

        val entity = StringEntity(requestBody, ContentType.APPLICATION_JSON)
        httpPost.entity = entity

        logger.debug("发送http请求到$apiUrl")

        httpClient.execute(httpPost).use { response ->
            val statusCode = response.statusLine.statusCode
            logger.debug("HTTP响应的状态码为$statusCode")

            if (statusCode != 200) {
                logger.error("HTTP响应失败，状态码为$statusCode")
                handler.onChunk("HTTP响应失败，状态码为$statusCode", true)
                return
            }

            response.entity.content.bufferedReader(Charsets.UTF_8).use { reader ->
                val partialData = StringBuilder()

                reader.forEachLine { line ->
                    if (line.startsWith("data: ")) {
                        val data = line.substring(6)

                        if ("[DONE]" == data) {
                            logger.debug("流式响应结束")
                            if (partialData.isNotEmpty()) {
                                handler.onChunk(partialData.toString(), true)
                                partialData.setLength(0)
                            } else {
                                handler.onChunk("", true)
                            }
                            return@forEachLine
                        }

                        try {
                            val jsonData = JSON.parseObject(data)

                            if (jsonData.containsKey("choices") && !jsonData.getJSONArray("choices").isEmpty()) {
                                val choice = jsonData.getJSONArray("choices").getJSONObject(0)

                                if (choice.containsKey("delta")) {
                                    val delta = choice.getJSONObject("delta")

                                    if (delta.containsKey("content")) {
                                        val content = delta.getString("content")
                                        handler.onChunk(content, false)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            logger.error("解析流式响应JSON出错", e)
                            handler.onChunk("解析响应出错: ${e.message}", false)
                        }
                    }
                }
            }
        }
    }

    override suspend fun chatStreamList(request: LlmRequest): List<String> {
        if (request.model == "default") {
            logger.info("未指定模型或使用默认模型，使用配置的默认模型: $defaultModel")
            request.model = defaultModel
        }

        return try {
            logger.info("发送流式请求到SiliconFlow服务, 模型:${request.model},消息数:${request.messages.size}")

            request.stream = true
            val requestBody = this.prepareRequestBody(request)

            val chunks = mutableListOf<String>()
            val channel = Channel<String>(Channel.UNLIMITED)

            val job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    sendStreamHttpRequest(requestBody, object : StreamResponseHandler {
                        override fun onChunk(chunk: String, isLast: Boolean) {
                            runBlocking {
                                channel.send(chunk)
                                if (isLast) {
                                    channel.close()
                                }
                            }
                        }
                    })
                } catch (e: Exception) {
                    channel.close()
                }
            }

            try {
                for (chunk in channel) {
                    chunks.add(chunk)
                }
            } catch (e: Exception) {
                job.cancel()
                throw e
            }

            job.join()

            logger.info("SiliconFlow流式响应完成，共返回 {} 个块", chunks.size)
            chunks.toList()

        } catch (e: Exception) {
            logger.error("调用SiliconFlow流式服务出错", e)
            listOf("调用流式服务时发生错误: ${e.message}")
        }
    }
}
