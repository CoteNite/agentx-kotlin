package cn.cotenite.application.conversation.service

import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import cn.cotenite.application.conversation.assembler.MessageAssembler
import cn.cotenite.application.conversation.dto.ChatRequest
import cn.cotenite.application.conversation.dto.MessageDTO
import cn.cotenite.domain.agent.service.AgentDomainService
import cn.cotenite.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.domain.conversation.handler.ChatEnvironment
import cn.cotenite.domain.conversation.handler.MessageHandlerFactory
import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.service.ContextDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.domain.conversation.service.SessionDomainService
import cn.cotenite.domain.llm.service.LlmDomainService
import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.TokenDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.transport.MessageTransportFactory
import java.time.LocalDateTime

/**
 * 对话应用服务
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
    private val messageHandlerFactory: MessageHandlerFactory,
    private val transportFactory: MessageTransportFactory
) {

    fun getConversationMessages(sessionId: String, userId: String): List<MessageDTO> {
        sessionDomainService.find(sessionId, userId) ?: throw BusinessException("会话不存在")
        return conversationDomainService.getConversationMessages(sessionId).let(MessageAssembler::toDTOs)
    }

    fun chat(chatRequest: ChatRequest, userId: String): SseEmitter {
        val environment = prepareEnvironment(chatRequest, userId)
        val transport = transportFactory.getSseTransport()
        val handler = messageHandlerFactory.getHandler(environment.agent)
        return handler.handleChat(environment, transport)
    }

    private fun prepareEnvironment(chatRequest: ChatRequest, userId: String): ChatEnvironment {
        val sessionId = chatRequest.sessionId ?: throw BusinessException("会话id不可为空")
        val userMessage = chatRequest.message ?: throw BusinessException("消息内容不可为空")

        val session = sessionDomainService.getSession(sessionId, userId)
        val agentId = session.agentId ?: throw BusinessException("会话未绑定助理")

        val agent = agentDomainService.getAgentById(agentId)
        if (agent.userId != userId && !agent.enabled) throw BusinessException("agent已被禁用")

        val workspace = agentWorkspaceDomainService.getWorkspace(agentId, userId)
        val llmModelConfig = workspace.llmModelConfig
        val modelId = llmModelConfig.modelId ?: throw BusinessException("未配置模型")

        val model = llmDomainService.getModelById(modelId).also { it.isActive() }
        val provider = llmDomainService.getProvider(model.providerId.orEmpty(), userId).also { it.isActive() }

        val contextEntity = contextDomainService.findBySessionId(sessionId) ?: ContextEntity().apply {
            this.sessionId = sessionId
        }

        val messageEntities = messageDomainService.listByIds(contextEntity.activeMessages)

        applyTokenOverflowStrategy(
            strategyType = llmModelConfig.strategyType,
            maxTokens = llmModelConfig.maxTokens,
            summaryThreshold = llmModelConfig.summaryThreshold,
            providerConfig = provider.protocol?.let { protocol ->
                ProviderConfig(provider.config?.apiKey, provider.config?.baseUrl, model.modelId, protocol)
            },
            contextEntity = contextEntity,
            messageEntities = messageEntities
        )

        return ChatEnvironment(
            sessionId = sessionId,
            userId = userId,
            userMessage = userMessage,
            agent = agent,
            model = model,
            provider = provider,
            llmModelConfig = llmModelConfig,
            contextEntity = contextEntity,
            messageHistory = messageEntities
        )
    }

    private fun applyTokenOverflowStrategy(
        strategyType: TokenOverflowStrategyEnum,
        maxTokens: Int?,
        summaryThreshold: Int?,
        providerConfig: ProviderConfig?,
        contextEntity: ContextEntity,
        messageEntities: List<MessageEntity>
    ) {
        val config = TokenOverflowConfig().apply {
            this.strategyType = strategyType
            this.maxTokens = maxTokens
            this.summaryThreshold = summaryThreshold
            this.providerConfig = providerConfig
        }

        tokenDomainService.processMessages(tokenizeMessages(messageEntities), config)
            .takeIf { it.processed }
            ?.also { result ->
                contextEntity.activeMessages = result.retainedMessages.mapNotNull(TokenMessage::id).toMutableList()

                if (strategyType == TokenOverflowStrategyEnum.SUMMARIZE && !result.summary.isNullOrBlank()) {
                    contextEntity.summary = contextEntity.summary.orEmpty() + result.summary
                }

                contextDomainService.insertOrUpdate(contextEntity)
            }
    }

    private fun tokenizeMessages(messageEntities: List<MessageEntity>): List<TokenMessage> =
        messageEntities.map {
            TokenMessage(
                id = it.id,
                role = it.role?.name,
                content = it.content,
                tokenCount = it.tokenCount,
                createdAt = it.createdAt ?: LocalDateTime.now()
            )
        }
}
