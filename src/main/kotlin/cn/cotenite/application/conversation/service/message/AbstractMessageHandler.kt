package cn.cotenite.application.conversation.service.message

import cn.cotenite.application.conversation.dto.AgentChatResponse
import cn.cotenite.application.conversation.service.handler.context.AgentPromptTemplates
import cn.cotenite.application.conversation.service.handler.context.ChatContext
import cn.cotenite.domain.conversation.constant.MessageType
import cn.cotenite.domain.conversation.constant.Role.ASSISTANT
import cn.cotenite.domain.conversation.constant.Role.USER
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import cn.cotenite.infrastructure.storage.OssUploadService
import cn.cotenite.infrastructure.transport.MessageTransport
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ImageContent
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.tool.ToolProvider
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore
import org.slf4j.LoggerFactory

/**
 * 消息处理抽象模板
 */
abstract class AbstractMessageHandler(
    protected open val llmServiceFactory: LLMServiceFactory,
    protected open val messageDomainService: MessageDomainService,
    protected open val ossUploadService: OssUploadService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        protected const val CONNECTION_TIMEOUT = 3_000_000L
    }

    fun <T> chat(chatContext: ChatContext, transport: MessageTransport<T>): T {
        val connection = transport.createConnection(CONNECTION_TIMEOUT)
        val model = llmServiceFactory.getStreamingClient(chatContext.provider, chatContext.model)
        val llmMessageEntity = createLlmMessage(chatContext)
        val userMessageEntity = createUserMessage(chatContext)

        val memory = initMemory().also { buildHistoryMessage(chatContext, it) }
        val agent = buildAgent(model, memory, provideTools(chatContext))
        processChat(agent, connection, transport, chatContext, userMessageEntity, llmMessageEntity)
        return connection
    }

    protected open fun <T> processChat(
        agent: Agent,
        connection: T,
        transport: MessageTransport<T>,
        chatContext: ChatContext,
        userEntity: MessageEntity,
        llmEntity: MessageEntity
    ) {
        agent.chat(chatContext.userMessage).apply {
            chatContext.contextEntity?.let { messageDomainService.saveMessageAndUpdateContext(listOf(userEntity), it) }
            var messageBuilder = StringBuilder()

            onError { throwable ->
                transport.sendMessage(connection,
                    AgentChatResponse.buildEndMessage(throwable.message?:"内部发生错误", MessageType.TEXT)
                )
            }

            onPartialResponse { reply ->
                messageBuilder.append(reply)
                transport.sendMessage(connection, AgentChatResponse.build(reply, MessageType.TEXT))
            }

            onCompleteResponse { chatResponse ->
                llmEntity.tokenCount = chatResponse.tokenUsage().outputTokenCount()
                llmEntity.content = chatResponse.aiMessage().text()
                userEntity.tokenCount = chatResponse.tokenUsage().inputTokenCount()

                messageDomainService.updateMessage(userEntity)
                chatContext.contextEntity?.let { messageDomainService.saveMessageAndUpdateContext(listOf(llmEntity), it) }
                transport.sendEndMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT))
            }

            onToolExecuted { toolExecution ->
                messageBuilder.takeIf { it.isNotEmpty() }?.toString()?.let { partialContent ->
                    transport.sendMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT))
                    llmEntity.content = partialContent
                    chatContext.contextEntity?.let { messageDomainService.saveMessageAndUpdateContext(listOf(llmEntity), it) }
                    messageBuilder = StringBuilder()
                }

                val message = "执行工具：${toolExecution.request().name()}"
                createLlmMessage(chatContext).apply {
                    messageType = MessageType.TOOL_CALL
                    content = message
                    chatContext.contextEntity?.let { messageDomainService.saveMessageAndUpdateContext(listOf(this), it) }
                }

                transport.sendMessage(connection, AgentChatResponse.buildEndMessage(message, MessageType.TOOL_CALL))
            }

            start()
        }
    }

    protected fun buildAgent(
        model: StreamingChatModel,
        memory: MessageWindowChatMemory,
        toolProvider: ToolProvider?
    ): Agent {
        val builder = AiServices.builder(Agent::class.java)
            .streamingChatModel(model)
            .chatMemory(memory)

        toolProvider?.let(builder::toolProvider)
        return builder.build()
    }

    protected fun initMemory(): MessageWindowChatMemory = MessageWindowChatMemory.builder()
        .maxMessages(1000)
        .chatMemoryStore(InMemoryChatMemoryStore())
        .build()

    protected fun buildHistoryMessage(chatContext: ChatContext, memory: MessageWindowChatMemory) {
        chatContext.contextEntity?.summary
            ?.takeIf { it.isNotBlank() }
            ?.let { summary -> memory.add(AiMessage(AgentPromptTemplates.SUMMARY_PREFIX + summary)) }

        var presetToolPrompt = ""

        // 设置预先工具设置的参数到系统提示词中
        val toolPresetParams = chatContext.agent.toolPresetParams
        if (toolPresetParams != null) {
            presetToolPrompt = AgentPromptTemplates.generatePresetToolPrompt(toolPresetParams)
        }

        memory.add(SystemMessage(chatContext.agent.systemPrompt + "\n" + presetToolPrompt))

        chatContext.messageHistory?.forEach { messageEntity ->
            when {
                messageEntity.isUserMessage() -> {
                    val contents = mutableListOf<dev.langchain4j.data.message.Content>()

                    // 图片：下载并转为 base64，避免 SiliconFlow 等外部 LLM 无法访问 localhost URL
                    messageEntity.fileUrls?.filterNotNull()?.forEach { fileUrl ->
                        runCatching { fetchImageAsBase64Content(fileUrl) }
                            .onSuccess { contents.add(it) }
                            .onFailure { logger.warn("图片加载失败，跳过: url={}, error={}", fileUrl, it.message) }
                    }

                    // 文本内容
                    val text = messageEntity.content
                    if (!text.isNullOrBlank()) {
                        contents.add(TextContent.from(text))
                    }

                    if (contents.isNotEmpty()) {
                        memory.add(UserMessage.from(contents))
                    }
                }

                messageEntity.isAIMessage() -> memory.add(AiMessage(messageEntity.content ?: ""))
                messageEntity.isSystemMessage() -> memory.add(SystemMessage(messageEntity.content ?: ""))
            }
        }
    }

    /**
     * 将图片 URL 下载并转换为 base64 编码的 ImageContent
     * 使用 OssUploadService（S3Client 认证）下载，解决私有 bucket 403 问题
     */
    private fun fetchImageAsBase64Content(imageUrl: String): ImageContent {
        val (base64Data, mimeType) = ossUploadService.downloadImageAsBase64(imageUrl)
        return ImageContent.from(base64Data, mimeType)
    }

    /** 从 Content-Type 头或文件扩展名推断 MIME 类型 */
    private fun detectMimeType(contentType: String?, url: String): String {
        if (!contentType.isNullOrBlank() && contentType.startsWith("image/")) {
            return contentType.substringBefore(";").trim()
        }
        return when (url.substringAfterLast('.').lowercase().substringBefore('?')) {
            "jpg", "jpeg" -> "image/jpeg"
            "png"         -> "image/png"
            "gif"         -> "image/gif"
            "webp"        -> "image/webp"
            "bmp"         -> "image/bmp"
            else          -> "image/jpeg"
        }
    }

    protected open fun provideTools(chatContext: ChatContext): ToolProvider? = null

    protected fun createLlmMessage(environment: ChatContext) = MessageEntity().apply {
        role = ASSISTANT
        sessionId = environment.sessionId
        model = environment.model.modelId
        provider = environment.provider.id
    }

    protected fun createUserMessage(environment: ChatContext) = MessageEntity().apply {
        role = USER
        content = environment.userMessage
        sessionId = environment.sessionId
        fileUrls=environment.fileUrls
    }

    protected fun <T> handleError(
        connection: T,
        transport: MessageTransport<T>,
        chatContext: ChatContext,
        message: String,
        llmEntity: MessageEntity,
        throwable: Throwable
    ) {
        llmEntity.tokenCount = OpenAiTokenCountEstimator(OpenAiChatModelName.GPT_4_O)
            .estimateTokenCountInMessage(AiMessage.from(message))
        llmEntity.content = message

        chatContext.contextEntity?.let { messageDomainService.saveMessageAndUpdateContext(listOf(llmEntity), it) }
        transport.sendEndMessage(
            connection,
            AgentChatResponse.buildEndMessage(throwable.message ?: "", MessageType.TEXT)
        )
    }
}
