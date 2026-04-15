package cn.cotenite.application.conversation.service

import cn.cotenite.application.conversation.assembler.MessageAssembler
import cn.cotenite.application.conversation.dto.AgentPreviewRequest
import cn.cotenite.application.conversation.dto.ChatRequest
import cn.cotenite.application.conversation.dto.MessageDTO
import cn.cotenite.application.conversation.service.handler.MessageHandlerFactory
import cn.cotenite.application.conversation.service.handler.context.ChatContext
import cn.cotenite.application.conversation.service.message.preview.PreviewMessageHandler
import cn.cotenite.application.user.service.UserSettingsAppService
import cn.cotenite.domain.agent.constant.AgentType
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.agent.service.AgentDomainService
import cn.cotenite.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.domain.conversation.constant.Role
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
import cn.cotenite.domain.tool.model.UserToolEntity
import cn.cotenite.domain.tool.service.UserToolDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.transport.MessageTransportFactory
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime

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
    private val transportFactory: MessageTransportFactory,
    private val userToolDomainService: UserToolDomainService,
    private val userSettingsAppService: UserSettingsAppService,
    private val previewMessageHandler: PreviewMessageHandler
) {

    fun getConversationMessages(sessionId: String, userId: String): List<MessageDTO> {
        sessionDomainService.find(sessionId, userId) ?: throw BusinessException("会话不存在")

        return conversationDomainService.getConversationMessages(sessionId)
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

        var toolIds= agent.toolIds


        // 在工作区中的助理会分为用户自己创建的和安装的助理，因此需要区分 agent，如果 agent 的 userId 等于当前用户则使用 agent，反之使用
        // agent_version
        if (!agent.userId.equals(userId)) {
            val latestAgentVersion = agentDomainService.getLatestAgentVersion(agentId)
            // 直接转换即可
            latestAgentVersion?.let { toolIds = it.toolIds }?:throw BusinessException("")
            BeanUtils.copyProperties(latestAgentVersion, agent)
        }


        // 校验工具的可用性
        val installTool = userToolDomainService.getInstallTool(toolIds, userId)


        // 获取 mcp server name
        val mcpServerNames = installTool.map(UserToolEntity::mcpServerName).toList()


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
            llmModelConfig = llmModelConfig,
            mcpServerName=mcpServerNames,
            fileUrls=chatRequest.fileUrls?.toMutableList()?:emptyList<String?>().toMutableList(),
            chatRequest=chatRequest
        )
    }

    private fun prepareChatContext(
        sessionId: String,
        userId: String,
        userMessage: String,
        agent: AgentEntity,
        model: ModelEntity,
        provider: ProviderEntity,
        llmModelConfig: LLMModelConfig,
        mcpServerName: List<String?>,
        fileUrls: MutableList<String?>,
        chatRequest: ChatRequest
    ): ChatContext
    {
        val contextEntity = contextDomainService.findBySessionId(sessionId) ?: ContextEntity().apply { this.sessionId = sessionId }

        val messageEntities = contextEntity.activeMessages.let { ids ->
            if (ids.isNotEmpty()) {
                messageDomainService.listByIds(ids).also {
                    applyTokenOverflowStrategy(llmModelConfig, provider, model.modelId!!, contextEntity, it)
                }
            } else emptyList()
        }.toMutableList()


        // 特殊处理当前对话的文件，因为在后续的对话中无法发送文件
        chatRequest.fileUrls?.isEmpty()?.let {
            if (!it) {
                val messageEntity = MessageEntity()
                messageEntity.role=Role.USER
                messageEntity.fileUrls=fileUrls.toMutableList()
                messageEntities.add(messageEntity)
            }
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
            messageHistory = messageEntities,
            mcpServerName=mcpServerName,
            fileUrls=fileUrls
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

    /** Agent预览功能 - 无需保存会话的对话体验 */
    fun previewAgent(previewRequest: AgentPreviewRequest, userId: String): SseEmitter {
        // 1. 准备预览环境
        val environment = preparePreviewEnvironment(previewRequest, userId)

        // 2. 获取传输方式
        val transport = transportFactory.getTransport<SseEmitter>(MessageTransportFactory.TRANSPORT_TYPE_SSE)

        // 3. 使用预览专用的消息处理器
        return previewMessageHandler.chat(environment, transport)
    }

    /** 准备预览对话环境 */
    private fun preparePreviewEnvironment(previewRequest: AgentPreviewRequest, userId: String): ChatContext {
        // 1. 创建虚拟Agent实体
        val virtualAgent = createVirtualAgent(previewRequest, userId)

        // 2. 获取模型信息：使用 Elvis 运算符处理默认值
        val modelId = previewRequest.modelId?.takeIf { it.isNotBlank() }
            ?: userSettingsAppService.getUserDefaultModelId(userId)
            ?: throw BusinessException("用户未设置默认模型，且预览请求中未指定模型")

        val model = llmDomainService.getModelById(modelId).apply { isActive() }

        // 3. 获取服务商信息
        val provider = llmDomainService.getProvider(model.providerId!!, userId).apply { isActive() }

        // 4. 处理工具配置
        val mcpServerNames = previewRequest.toolIds?.takeIf { it.isNotEmpty() }?.let { ids ->
            userToolDomainService.getInstallTool(ids, userId).map { it.mcpServerName }
        } ?: emptyList()

        // 5 & 6. 创建并初始化环境对象
        return ChatContext(
            sessionId = "preview-session",
            userId = userId,
            userMessage = previewRequest.userMessage!!,
            agent = virtualAgent,
            model = model,
            provider = provider,
            llmModelConfig = createDefaultLLMModelConfig(modelId),
            mcpServerName = mcpServerNames,
            fileUrls = previewRequest.fileUrls
        ).apply {
            // 7. 设置预览上下文和历史消息
            setupPreviewContextAndHistory(this, previewRequest)
        }
    }

    /** 创建虚拟Agent实体 */
    private fun createVirtualAgent(previewRequest: AgentPreviewRequest, userId: String) = AgentEntity().apply {
        id = "preview-agent"
        this.userId = userId
        name = "预览助理"
        systemPrompt = previewRequest.systemPrompt
        toolIds = previewRequest.toolIds?.toMutableList()?:emptyList<String>().toMutableList()
        toolPresetParams = previewRequest.toolPresetParams
        agentType = AgentType.CHAT_ASSISTANT.code
        enabled = true
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    /** 创建默认的LLM模型配置 */
    private fun createDefaultLLMModelConfig(modelId: String) = LLMModelConfig().apply {
        this.modelId = modelId
        temperature = 0.7
        topP = 0.9
        maxTokens = 4000
        strategyType = TokenOverflowStrategyEnum.NONE
        summaryThreshold = 2000
    }

    /** 设置预览上下文和历史消息 */
    private fun setupPreviewContextAndHistory(environment: ChatContext, previewRequest: AgentPreviewRequest) {
        val contextEntity = ContextEntity().apply {
            sessionId = "preview-session"
            activeMessages = mutableListOf()
        }

        // 转换前端传入的历史消息
        val messageEntities = previewRequest.messageHistory?.map { dto ->
            MessageEntity().apply {
                id = dto.id
                role = dto.role
                content = dto.content
                sessionId = "preview-session"
                createdAt = dto.createdAt
                fileUrls = dto.fileUrls
                tokenCount = if (dto.role == Role.USER) 50 else 100
            }
        }?.toMutableList() ?: mutableListOf()

        // 特殊处理当前对话的文件
        previewRequest.fileUrls.takeIf { it.isNotEmpty() }?.let { urls ->
            messageEntities.add(MessageEntity().apply {
                role = Role.USER
                sessionId = "preview-session"
                fileUrls = urls.toMutableList()
            })
        }

        environment.contextEntity = contextEntity
        environment.messageHistory = messageEntities
    }
}