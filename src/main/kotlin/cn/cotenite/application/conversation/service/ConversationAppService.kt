package cn.cotenite.application.conversation.service

import cn.cotenite.application.conversation.assembler.MessageAssembler
import cn.cotenite.application.conversation.dto.AgentPreviewRequest
import cn.cotenite.application.conversation.dto.ChatRequest
import cn.cotenite.application.conversation.dto.MessageDTO
import cn.cotenite.application.conversation.service.handler.MessageHandlerFactory
import cn.cotenite.application.conversation.service.handler.context.ChatContext
import cn.cotenite.application.conversation.service.message.preview.PreviewMessageHandler
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
import cn.cotenite.domain.llm.service.HighAvailabilityDomainService
import cn.cotenite.domain.llm.service.LlmDomainService
import cn.cotenite.domain.shared.enums.TokenOverflowStrategyEnum
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.domain.token.service.TokenDomainService
import cn.cotenite.domain.tool.model.UserToolEntity
import cn.cotenite.domain.tool.service.UserToolDomainService
import cn.cotenite.domain.user.service.UserSettingsDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.config.ProviderConfig
import cn.cotenite.infrastructure.transport.MessageTransportFactory
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime

/** 对话应用服务，用于适配域层的对话服务 */
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
    private val userSettingsDomainService: UserSettingsDomainService,
    private val previewMessageHandler: PreviewMessageHandler,
    private val highAvailabilityDomainService: HighAvailabilityDomainService
) {

    /** 获取会话中的消息列表
     *
     * @param sessionId 会话id
     * @param userId 用户id
     * @return 消息列表 */
    fun getConversationMessages(sessionId: String, userId: String): List<MessageDTO> {
        // 查询对应会话是否存在
        val sessionEntity = sessionDomainService.find(sessionId, userId) ?: throw BusinessException("会话不存在")

        val conversationMessages = conversationDomainService.getConversationMessages(sessionId)
        return MessageAssembler.toDTOs(conversationMessages)
    }

    /** 对话方法 - 统一入口
     *
     * @param chatRequest 聊天请求
     * @param userId 用户ID
     * @return SSE发射器 */
    fun chat(chatRequest: ChatRequest, userId: String): SseEmitter {
        // 1. 准备对话环境
        val environment = prepareEnvironment(chatRequest, userId)

        // 2. 获取传输方式 (当前仅支持SSE，将来支持WebSocket)
        val transport = transportFactory.getTransport<SseEmitter>(MessageTransportFactory.TRANSPORT_TYPE_SSE)

        // 3. 获取适合的消息处理器 (根据agent类型)
        val handler = messageHandlerFactory.getHandler(environment.agent)

        // 4. 处理对话
        return handler.chat(environment, transport)
    }

    /** 准备对话环境
     *
     * @param chatRequest 聊天请求
     * @param userId 用户ID
     * @return 对话环境 */
    private fun prepareEnvironment(chatRequest: ChatRequest, userId: String): ChatContext {
        // 1. 获取会话
        val sessionId = chatRequest.sessionId
        val session = sessionDomainService.getSession(sessionId, userId)
        val agentId = session.agentId

        // 2. 获取对应agent
        val agent = agentDomainService.getAgentById(agentId!!)
        if (!agent.userId.equals(userId) && !agent.enabled) {
            throw BusinessException("agent已被禁用")
        }

        var toolIds = agent.toolIds

        // 在工作区中的助理会分为用户自己创建的和安装的助理，因此需要区分 agent，如果 agent 的 userId 等于当前用户则使用 agent，反之使用
        // agent_version
        if (!agent.userId.equals(userId)) {
            val latestAgentVersion = agentDomainService.getLatestAgentVersion(agentId)
            // 直接转换即可
            toolIds = latestAgentVersion?.toolIds ?: mutableListOf()
            if (latestAgentVersion!=null){
                BeanUtils.copyProperties(latestAgentVersion, agent)
            }
        }

        // 校验工具的可用性
        val installTool = userToolDomainService.getInstallTool(toolIds, userId)

        // 获取 mcp server name
        val mcpServerNames = installTool.map(UserToolEntity::mcpServerName)

        // 3. 获取工作区和模型配置
        val workspace = agentWorkspaceDomainService.getWorkspace(agentId, userId)
        val llmModelConfig = workspace.llmModelConfig
        val modelId = llmModelConfig.modelId
        val model = llmDomainService.getModelById(modelId!!)
        model.isActive()

        // 4. 获取用户降级配置
        val fallbackChain = userSettingsDomainService.getUserFallbackChain(userId)

        // 5. 获取服务商信息（支持高可用、会话亲和性和降级）
        val result = highAvailabilityDomainService.selectBestProvider(model, userId, sessionId, fallbackChain)
        val provider = result.provider
        val selectedModel = result.model // 可能是不同的部署名称
        val instanceId = result.instanceId // 获取实例ID
        provider?.isActive()

        // 5. 创建环境对象
        val chatContext = ChatContext(
            sessionId = sessionId,
            userId = userId,
            userMessage = chatRequest.message,
            agent = agent,
            model = selectedModel!!,
            provider = provider!!,
            llmModelConfig = llmModelConfig,
            mcpServerName = mcpServerNames,
            fileUrls = chatRequest.fileUrls?.toMutableList(),
            instanceId = instanceId
        )
        // 6. 设置上下文信息和消息历史
        setupContextAndHistory(chatContext, chatRequest)

        return chatContext
    }

    /** 设置上下文和历史消息
     *
     * @param environment 对话环境 */
    private fun setupContextAndHistory(environment: ChatContext, chatRequest: ChatRequest) {
        val sessionId = environment.sessionId

        // 获取上下文
        var contextEntity = contextDomainService.findBySessionId(sessionId)
        var messageEntities = mutableListOf<MessageEntity>()

        if (contextEntity != null) {
            // 获取活跃消息
            val activeMessageIds = contextEntity.activeMessages
            messageEntities = messageDomainService.listByIds(activeMessageIds).toMutableList()

            // 应用Token溢出策略
            applyTokenOverflowStrategy(environment, contextEntity, messageEntities)
        } else {
            contextEntity = ContextEntity()
            contextEntity.sessionId = sessionId
        }

        // 特殊处理当前对话的文件，因为在后续的对话中无法发送文件
        val fileUrls = chatRequest.fileUrls
        if (!fileUrls.isNullOrEmpty()) {
            val messageEntity = MessageEntity()
            messageEntity.role = Role.USER
            messageEntity.fileUrls = fileUrls.toMutableList()
            messageEntities.add(messageEntity)
        }

        environment.contextEntity = contextEntity
        environment.messageHistory = messageEntities
    }

    /** 应用Token溢出策略
     *
     * @param environment 对话环境
     * @param contextEntity 上下文实体
     * @param messageEntities 消息实体列表 */
    private fun applyTokenOverflowStrategy(
        environment: ChatContext,
        contextEntity: ContextEntity,
        messageEntities: List<MessageEntity>
    ) {
        val llmModelConfig = environment.llmModelConfig
        val provider = environment.provider

        // 处理Token溢出
        val strategyType = llmModelConfig.strategyType

        // Token处理
        val tokenMessages = tokenizeMessage(messageEntities)

        // 构造Token配置
        val tokenOverflowConfig = TokenOverflowConfig()
        tokenOverflowConfig.strategyType = strategyType
        tokenOverflowConfig.maxTokens = llmModelConfig.maxTokens
        tokenOverflowConfig.summaryThreshold = llmModelConfig.summaryThreshold

        // 设置提供商配置
        val providerConfig = provider.config
        tokenOverflowConfig.providerConfig = ProviderConfig(
            providerConfig?.apiKey,
            providerConfig?.baseUrl,
            environment.model.modelId,
            provider.protocol ?: throw BusinessException("协议不存在")
        )

        // 处理Token
        val result = tokenDomainService.processMessages(tokenMessages, tokenOverflowConfig)

        // 更新上下文
        if (result.processed) {
            val retainedMessages = result.retainedMessages
            val retainedMessageIds = retainedMessages.map { it.id }.toMutableList()

            if (strategyType == TokenOverflowStrategyEnum.SUMMARIZE) {
                val newSummary = result.summary
                val oldSummary = contextEntity.summary
                contextEntity.summary = oldSummary + newSummary
            }

            contextEntity.activeMessages = retainedMessageIds
        }
    }

    /** 消息实体转换为token消息 */
    private fun tokenizeMessage(messageEntities: List<MessageEntity>): List<TokenMessage> {
        return messageEntities.map { message ->
            val tokenMessage = TokenMessage()
            tokenMessage.id = message.id
            tokenMessage.role = message.role?.name
            tokenMessage.content = message.content
            tokenMessage.tokenCount = message.tokenCount
            tokenMessage.createdAt = message.createdAt!!
            tokenMessage
        }
    }

    /** Agent预览功能 - 无需保存会话的对话体验
     *
     * @param previewRequest 预览请求
     * @param userId 用户ID
     * @return SSE发射器 */
    fun previewAgent(previewRequest: AgentPreviewRequest, userId: String): SseEmitter {
        // 1. 准备预览环境
        val environment = preparePreviewEnvironment(previewRequest, userId)

        // 2. 获取传输方式
        val transport = transportFactory.getTransport<SseEmitter>(MessageTransportFactory.TRANSPORT_TYPE_SSE)

        // 3. 使用预览专用的消息处理器
        return previewMessageHandler.chat(environment, transport)
    }

    /** 准备预览对话环境
     *
     * @param previewRequest 预览请求
     * @param userId 用户ID
     * @return 预览对话环境 */
    private fun preparePreviewEnvironment(previewRequest: AgentPreviewRequest, userId: String): ChatContext {
        // 1. 创建虚拟Agent实体
        val virtualAgent = createVirtualAgent(previewRequest, userId)

        // 2. 获取模型信息
        var modelId = previewRequest.modelId
        if (modelId == null || modelId.trim().isEmpty()) {
            // 使用用户默认模型
            modelId = userSettingsDomainService.getUserDefaultModelId(userId)
            if (modelId == null) {
                throw BusinessException("用户未设置默认模型，且预览请求中未指定模型")
            }
        }

        val model = llmDomainService.getModelById(modelId)
        model.isActive()

        // 3. 获取服务商信息
        val provider = llmDomainService.getProvider(model.providerId!!, userId)
        provider.isActive()

        // 4. 处理工具配置
        val toolIds = previewRequest.toolIds
        var mcpServerNames = mutableListOf<String>()
        if (!toolIds.isNullOrEmpty()) {
            val installTool = userToolDomainService.getInstallTool(toolIds, userId)
            mcpServerNames = installTool.map(UserToolEntity::mcpServerName).filterNotNull().toMutableList()
        }

        // 5. 创建默认的LLM模型配置
        val llmModelConfig = createDefaultLLMModelConfig(modelId)

        // 6. 创建环境对象
        val chatContext = ChatContext(
            sessionId = "preview-session",
            userId = userId,
            userMessage = previewRequest.userMessage ?: "",
            agent = virtualAgent,
            model = model,
            provider = provider,
            llmModelConfig = llmModelConfig,
            mcpServerName = mcpServerNames,
            fileUrls = previewRequest.fileUrls?.toMutableList()
        )

        // 7. 设置预览上下文和历史消息
        setupPreviewContextAndHistory(chatContext, previewRequest)

        return chatContext
    }

    /** 创建虚拟Agent实体 */
    private fun createVirtualAgent(previewRequest: AgentPreviewRequest, userId: String): AgentEntity {
        val virtualAgent = AgentEntity()
        virtualAgent.id = "preview-agent"
        virtualAgent.userId = userId
        virtualAgent.name = "预览助理"
        virtualAgent.systemPrompt = previewRequest.systemPrompt
        virtualAgent.toolIds = previewRequest.toolIds?.toMutableList() ?: mutableListOf()
        virtualAgent.toolPresetParams = previewRequest.toolPresetParams
        virtualAgent.enabled = true
        virtualAgent.createdAt = LocalDateTime.now()
        virtualAgent.updatedAt = LocalDateTime.now()
        return virtualAgent
    }

    /** 创建默认的LLM模型配置 */
    private fun createDefaultLLMModelConfig(modelId: String): LLMModelConfig {
        val llmModelConfig = LLMModelConfig()
        llmModelConfig.modelId = modelId
        llmModelConfig.temperature = 0.7
        llmModelConfig.topP = 0.9
        llmModelConfig.maxTokens = 4000
        llmModelConfig.strategyType = TokenOverflowStrategyEnum.NONE
        llmModelConfig.summaryThreshold = 2000
        return llmModelConfig
    }

    /** 设置预览上下文和历史消息 */
    private fun setupPreviewContextAndHistory(environment: ChatContext, previewRequest: AgentPreviewRequest) {
        // 创建虚拟上下文实体
        val contextEntity = ContextEntity()
        contextEntity.sessionId = "preview-session"
        contextEntity.activeMessages = mutableListOf()

        // 转换前端传入的历史消息为实体
        val messageEntities = mutableListOf<MessageEntity>()
        val messageHistory = previewRequest.messageHistory
        if (!messageHistory.isNullOrEmpty()) {
            for (messageDTO in messageHistory) {
                val messageEntity = MessageEntity()
                messageEntity.id = messageDTO.id
                messageEntity.role = messageDTO.role
                messageEntity.content = messageDTO.content
                messageEntity.sessionId = "preview-session"
                messageEntity.createdAt = messageDTO.createdAt
                messageEntity.fileUrls = messageDTO.fileUrls
                messageEntity.tokenCount = if (messageDTO.role == Role.USER) 50 else 100 // 预估token数
                messageEntities.add(messageEntity)
            }
        }

        // 特殊处理当前对话的文件，因为在后续的对话中无法发送文件
        val fileUrls = previewRequest.fileUrls
        if (!fileUrls.isEmpty()) {
            val messageEntity = MessageEntity()
            messageEntity.role = Role.USER
            messageEntity.sessionId = "preview-session"
            messageEntity.fileUrls = fileUrls.toMutableList()
            messageEntities.add(messageEntity)
        }

        environment.contextEntity = contextEntity
        environment.messageHistory = messageEntities
    }
}
