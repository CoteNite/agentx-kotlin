package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.MessageDTO
import cn.cotenite.agentxkotlin.domain.llm.service.LlmService
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:32
 */
interface ConversationService {

    /**
     * 发送消息并流式获取回复
     *
     * @param sessionId 会话ID
     * @param content   用户消息内容
     * @return SSE事件发射器
     */
    fun chat(sessionId: String, content: String): SseEmitter

    /**
     * 发送消息并获取回复（非流式）
     *
     * @param sessionId 会话ID
     * @param content   用户消息内容
     * @return 助手回复消息
     */
    fun chatSync(sessionId: String, content: String): MessageDTO

    /**
     * 创建新会话并发送第一条消息
     *
     * @param title   会话标题
     * @param userId  用户ID
     * @param content 用户消息内容
     * @return SSE事件发射器
     */
    fun createSessionAndChat(title: String, userId: String, content: String): SseEmitter

    /**
     * 清除会话上下文
     *
     * @param sessionId 会话ID
     */
    fun clearContext(sessionId: String)

}

class ConversationServiceImpl(
    private val sessionService: SessionService,
    private val contextService: ContextService,
    private val llmService: LlmService,
) : ConversationService {

    companion object{
        private const val DEFAULT_SYSTEM_PROMPT: String = "你是一个有帮助的AI助手，请尽可能准确、有用地回答用户问题。"
    }

    override fun chat(sessionId: String, content: String): SseEmitter {
        TODO("Not yet implemented")
    }

    override fun chatSync(sessionId: String, content: String): MessageDTO {
        TODO("Not yet implemented")
    }

    override fun createSessionAndChat(title: String, userId: String, content: String): SseEmitter {
        TODO("Not yet implemented")
    }

    override fun clearContext(sessionId: String) {
        TODO("Not yet implemented")
    }
}
