package cn.cotenite.agentxkotlin.application.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.MessageDTO
import cn.cotenite.agentxkotlin.domain.conversation.service.MessageService
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/19 00:24
 */
@Service
class MessageAppService(
    private val messageService: MessageService
){

    /**
     * 发送用户消息
     */
    fun sendUserMessage(sessionId: String, content: String): MessageDTO {
        return messageService.sendUserMessage(sessionId, content)
    }

    /**
     * 保存助手消息
     */
    fun saveAssistantMessage(sessionId: String, content: String, provider: String, model: String,
                             tokenCount: Int): MessageDTO {
        return messageService.saveAssistantMessage(sessionId, content, provider, model, tokenCount)
    }

    /**
     * 保存系统消息
     */
    fun saveSystemMessage(sessionId: String, content: String): MessageDTO {
        return messageService.saveSystemMessage(sessionId, content)
    }

    /**
     * 获取会话消息列表
     */
    fun getSessionMessages(sessionId: String): List<MessageDTO> {
        return messageService.getSessionMessages(sessionId)
    }

    /**
     * 获取会话最近消息
     */
    fun getRecentMessages(sessionId: String, count: Int): List<MessageDTO> {
        return messageService.getRecentMessages(sessionId, count)
    }

    /**
     * 删除消息
     */
    fun deleteMessage(messageId: String) {
        messageService.deleteMessage(messageId)
    }

    /**
     * 删除会话所有消息
     */
    fun deleteSessionMessages(sessionId: String) {
        messageService.deleteSessionMessages(sessionId)
    }

}
