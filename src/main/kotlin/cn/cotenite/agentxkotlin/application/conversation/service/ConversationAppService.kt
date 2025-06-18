package cn.cotenite.agentxkotlin.application.conversation.service

import cn.cotenite.agentxkotlin.application.conversation.dto.ChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.ChatResponse
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageDTO
import cn.cotenite.agentxkotlin.domain.conversation.service.ConversationService
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.function.BiConsumer


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/19 00:22
 */
@Service
class ConversationAppService(
    private val conversationService: ConversationService,
    private val applicationConversationService: cn.cotenite.agentxkotlin.application.conversation.service.ConversationService
){


    /**
     * 发送消息并获取流式回复
     */
    fun chat(sessionId: String, content: String): SseEmitter {
        return conversationService.chat(sessionId, content)
    }

    /**
     * 发送消息并获取同步回复（非流式）
     */
    fun chatSync(sessionId: String, content: String): MessageDTO {
        return conversationService.chatSync(sessionId, content)
    }

    /**
     * 创建新会话并发送第一条消息
     */
    fun createSessionAndChat(title: String, userId: String, content: String): SseEmitter {
        return conversationService.createSessionAndChat(title, userId, content)
    }

    /**
     * 清除会话上下文
     */
    fun clearContext(sessionId: String) {
        conversationService.clearContext(sessionId)
    }

    /**
     * 处理聊天请求
     */
    fun chat(request: ChatRequest): ChatResponse {
        return applicationConversationService.chat(request)
    }

    /**
     * 处理流式聊天请求
     */
    suspend fun chatStream(request: StreamChatRequest, responseHandler: (chunk: StreamChatResponse, isLast: Boolean) -> Unit) {
        applicationConversationService.chatStream(request, responseHandler)
    }
}
