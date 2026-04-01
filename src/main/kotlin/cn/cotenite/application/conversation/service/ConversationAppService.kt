package cn.cotenite.application.conversation.service

import cn.cotenite.application.conversation.assembler.MessageAssembler
import cn.cotenite.application.conversation.dto.ChatRequest
import cn.cotenite.application.conversation.dto.MessageDTO
import cn.cotenite.application.conversation.service.handler.MessageHandlerFactory
import cn.cotenite.application.conversation.service.handler.context.ChatContext
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.agent.service.AgentDomainService
import cn.cotenite.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.service.ContextDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.domain.conversation.service.SessionDomainService
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.domain.llm.service.LlmDomainService
import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.TokenDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.transport.MessageTransportFactory
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * 对话应用服务
 */
/**
 * 对话应用服务，使用 Kotlin 函数式风格重写
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
        val sessionEntity = sessionDomainService.find(sessionId, userId)
            ?: throw BusinessException("会话不存在")

        return conversationDomainService.getConversationMessages(sessionEntity.id)
            .let { MessageAssembler.toDTOs(it) }
    }

    fun chat(chatRequest: ChatRequest, userId: String): SseEmitter {
        // 1. 准备环境
        val environment = prepareEnvironment(chatRequest, userId)

        // 2. 获取传输与处理器并执行 (链式调用)
        val transport = transportFactory.getTransport<SseEmitter>(MessageTransportFactory.TRANSPORT_TYPE_SSE)
        val handler = messageHandlerFactory.getHandler(environment.agent)

        return handler.chat(environment, transport)
    }

    private fun prepareEnvironment(chatRequest: ChatRequest, userId: String): ChatContext {
        val sessionId = chatRequest.sessionId
        val session = sessionDomainService.getSession(sessionId, userId)
        val agentId = session.agentId?:throw BusinessException("Agent不存在")

        val agent = agentDomainService.getAgentById(agentId).apply {
            if (this.userId != userId && !this.enabled) throw BusinessException("agent已被禁用")
        }

        val workspace = agentWorkspaceDomainService.getWorkspace(agentId, userId)
        val llmModelConfig = workspace.llmModelConfig
        val model = llmDomainService.getModelById(llmModelConfig.modelId?:throw BusinessException("模型不存在")).apply { isActive() }
        val provider = llmDomainService.getProvider(model.providerId?:throw BusinessException("模型提供商不存在"), userId).apply { isActive() }

        return prepareChatContext(
            sessionId = sessionId,
            userId = userId,
            userMessage = chatRequest.message,
            agent = agent,
            model = model,
            provider = provider,
            llmModelConfig = llmModelConfig
        )
    }

    private fun prepareChatContext(
        sessionId: String,
        userId: String,
        userMessage: String,
        agent: AgentEntity,
        model: ModelEntity,
        provider: ProviderEntity,
        llmModelConfig: LLMModelConfig,): ChatContext
    {


        val contextEntity = contextDomainService.findBySessionId(sessionId) ?: ContextEntity().apply { this.sessionId = sessionId }

        val messageEntities = contextEntity.activeMessages.let { ids ->
            if (ids.isNotEmpty()) {
                messageDomainService.listByIds(ids).also {
                    applyTokenOverflowStrategy(llmModelConfig, provider, model.modelId!!, contextEntity, it)
                }
            } else emptyList()
        }

        return ChatContext(
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
        llmModelConfig: LLMModelConfig,
        provider: ProviderEntity,
        modelId: String,
        contextEntity: ContextEntity,
        messageEntities: List<MessageEntity>
    ) {

        val strategyType = llmModelConfig.strategyType

        val tokenOverflowConfig = TokenOverflowConfig().apply {
            this.strategyType = strategyType
            this.maxTokens = llmModelConfig.maxTokens
            this.summaryThreshold = llmModelConfig.summaryThreshold
            this.providerConfig = ProviderConfig(
                provider.config?.apiKey,
                provider.config?.baseUrl,
                modelId,
                provider.protocol?:throw BusinessException("协议不存在")
            )
        }

        tokenDomainService.processMessages(tokenizeMessage(messageEntities), tokenOverflowConfig)
            .takeIf { it.processed }
            ?.let { result ->
                contextEntity.activeMessages = result.retainedMessages.map { it.id }.toMutableList()

                if (strategyType == TokenOverflowStrategyEnum.SUMMARIZE) {
                    contextEntity.summary = (contextEntity.summary ?: "") + result.summary
                }
            }
    }

    private fun tokenizeMessage(messageEntities: List<MessageEntity>): List<TokenMessage> =
        messageEntities.map { message ->
            TokenMessage().apply {
                id = message.id
                role = message.role?.name
                content = message.content
                tokenCount = message.tokenCount
                createdAt = message.createdAt!!
            }
        }
}