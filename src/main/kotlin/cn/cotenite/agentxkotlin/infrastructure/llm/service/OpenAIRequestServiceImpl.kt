package cn.cotenite.agentxkotlin.infrastructure.llm.service

import cn.cotenite.agentxkotlin.domain.conversation.constant.Role
import cn.cotenite.agentxkotlin.domain.conversation.model.ContextEntity
import cn.cotenite.agentxkotlin.domain.conversation.service.ContextProcessor
import cn.cotenite.agentxkotlin.domain.llm.model.LLMRequest
import cn.cotenite.agentxkotlin.domain.llm.service.LLMRequestService
import org.springframework.stereotype.Component
import java.lang.Double
import kotlin.Float
import kotlin.String


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 03:05
 */
@Component
class OpenAIRequestServiceImpl: LLMRequestService {

    override fun buildRequest(
        contextResult: ContextProcessor.ContextResult,
        userMessage: String,
        systemPrompt: String,
        modelId: String,
        temperature: Float,
        topP: Float
    ): LLMRequest {
        val messages = ArrayList<LLMRequest.LLMMessage>()

        for (messageEntity in contextResult.messageEntities) {
            val role = messageEntity.role
            val content= messageEntity.content?:""

            if (role === Role.USER) {
                messages.add(LLMRequest.LLMMessage(LLMRequest.MessageType.USER, content))
            } else if (role === Role.SYSTEM) {
                messages.add(LLMRequest.LLMMessage(LLMRequest.MessageType.ASSISTANT, content))
            }
        }

        val contextEntity= contextResult.contextEntity
        if (contextEntity.summary?.isNotEmpty() == true) {
            val preStr = "以下消息是用户之前的历史消息精炼成的摘要消息："
            messages.add(LLMRequest.LLMMessage(LLMRequest.MessageType.ASSISTANT,  preStr + contextEntity.summary))
        }
        messages.add(LLMRequest.LLMMessage(LLMRequest.MessageType.USER, userMessage))


        // 添加系统提示语
        if (systemPrompt.isNotEmpty()) {
            messages.add(LLMRequest.LLMMessage(LLMRequest.MessageType.SYSTEM, systemPrompt))
        }


        val parameters= LLMRequest.LLMRequestParameters(modelId, temperature.toDouble(), topP.toDouble())

        // 构建并返回请求
        return LLMRequest(messages, parameters)
    }

}