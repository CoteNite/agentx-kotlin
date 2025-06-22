package cn.cotenite.agentxkotlin.application.conversation.service

import cn.cotenite.agentxkotlin.application.conversation.assmebler.MessageAssembler
import cn.cotenite.agentxkotlin.application.conversation.dto.ChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.MessageDTO
import cn.cotenite.agentxkotlin.application.conversation.dto.StreamChatResponse
import cn.cotenite.agentxkotlin.domain.agent.service.AgentDomainService
import cn.cotenite.agentxkotlin.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.agentxkotlin.domain.conversation.constant.Role
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import cn.cotenite.agentxkotlin.domain.conversation.service.ContextDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.ConversationDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.MessageDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.SessionDomainService
import cn.cotenite.agentxkotlin.domain.llm.service.LlmDomainService
import cn.cotenite.agentxkotlin.domain.token.service.TokenDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.infrastructure.llm.LLMProviderService
import cn.cotenite.agentxkotlin.infrastructure.llm.config.ProviderConfig
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

/**
 * 对话应用服务，用于适配域层的对话服务
 */
@Service
class ConversationAppService(
    private val conversationDomainService: ConversationDomainService,
    private val sessionDomainService: SessionDomainService,
    private val agentDomainService: AgentDomainService,
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val llmDomainService: LlmDomainService,
    private val contextDomainService: ContextDomainService,
    private val tokenDomainService: TokenDomainService,
    private val messageDomainService: MessageDomainService,

) {

    /**
     * 获取会话中的消息列表
     *
     * @param sessionId 会话id
     * @param userId    用户id
     * @return 消息列表
     */
    fun getConversationMessages(sessionId: String, userId: String): List<MessageDTO> {
        // 查询对应会话是否存在
        sessionDomainService.find(sessionId, userId) ?: throw BusinessException("会话不存在")

        val conversationMessages = conversationDomainService.getConversationMessages(sessionId)
        return MessageAssembler.toDTOs(conversationMessages)
    }

    fun chat(chatRequest: ChatRequest, userId: String): SseEmitter {
        TODO()
    }
}