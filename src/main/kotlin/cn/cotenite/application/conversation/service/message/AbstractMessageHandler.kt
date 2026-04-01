package cn.cotenite.application.conversation.service.message

import cn.cotenite.application.conversation.dto.AgentChatResponse
import cn.cotenite.application.conversation.service.handler.context.AgentPromptTemplate
import cn.cotenite.application.conversation.service.handler.context.ChatContext
import cn.cotenite.domain.conversation.constant.MessageType
import cn.cotenite.domain.conversation.constant.Role.*
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import cn.cotenite.infrastructure.transport.MessageTransport
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.tool.ToolProvider
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore


/**
 * @author  yhk
 * Description  
 * Date  2026/3/28 21:22
 */
abstract class AbstractMessageHandler(
    protected open val llmServiceFactory: LLMServiceFactory,
    protected open val messageDomainService: MessageDomainService
){

    companion object {
        protected const val CONNECTION_TIMEOUT = 3000000L
    }

    fun <T> chat(chatContext: ChatContext,transport: MessageTransport<T>):T{

        val connection = transport.createConnection(CONNECTION_TIMEOUT)

        val model = llmServiceFactory.getStreamingClient(chatContext.provider, chatContext.model)

        val llmMessageEntity = createLlmMessage(chatContext)
        val userMessageEntity = createUserMessage(chatContext)

        messageDomainService.saveMessageAndUpdateContext(listOf(userMessageEntity), chatContext.contextEntity)

        val memory=initMemory()

        buildHistoryMessage(chatContext,memory)

        val toolProvider = provideTools()

        val agent = buildAgent(model, memory, toolProvider)

        processChat(agent,connection,transport,chatContext,userMessageEntity,llmMessageEntity)

        return connection
    }

    private fun <T> processChat(
        agent: Agent,
        connection: T,
        transport: MessageTransport<T>,
        chatContext: ChatContext,
        userEntity: MessageEntity,
        llmEntity: MessageEntity
    ){
        agent.chat(chatContext.userMessage).apply {
            var messageBuilder = StringBuilder()
            ignoreErrors()
            onPartialResponse { reply ->
                messageBuilder.append(reply)
                transport.sendMessage(connection, AgentChatResponse.build(reply, MessageType.TEXT))
            }


            onCompleteResponse { chatResponse ->
                llmEntity.apply {
                    tokenCount = chatResponse.tokenUsage().outputTokenCount()
                    content = chatResponse.aiMessage().text()
                }

                userEntity.tokenCount = chatResponse.tokenUsage().inputTokenCount()

                messageDomainService.saveMessageAndUpdateContext(listOf(llmEntity), chatContext.contextEntity)
                transport.sendEndMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT))
            }

            onToolExecuted { toolExecution ->
                messageBuilder.takeIf { it.isNotEmpty() }?.let {
                    transport.sendMessage(connection, AgentChatResponse.buildEndMessage(MessageType.TEXT))
                    llmEntity.content=it.toString()
                    messageDomainService.saveMessageAndUpdateContext(listOf(llmEntity),chatContext.contextEntity)
                    messageBuilder = StringBuilder()
                }

                val message="执行工具：${toolExecution.request().name()}"

                createLlmMessage(chatContext).apply {
                    messageType= MessageType.TOOL_CALL
                    content=message
                    messageDomainService.saveMessageAndUpdateContext(listOf(this),chatContext.contextEntity)
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
    ): Agent{
        val agentService = AiServices.builder(Agent::class.java)
            .streamingChatModel(model)
            .chatMemory(memory)

        toolProvider?.let { agentService.toolProvider(it) }

        return agentService.build()
    }

    protected fun initMemory(): MessageWindowChatMemory = MessageWindowChatMemory.builder()
        .maxMessages(1000)
        .chatMemoryStore(InMemoryChatMemoryStore())
        .build()

    protected fun buildHistoryMessage(chatContext: ChatContext,memory: MessageWindowChatMemory){
        chatContext.contextEntity.summary?.takeIf { it.isNotEmpty() }.let {
            memory.add(AiMessage(AgentPromptTemplate.SUMMARY_PREFIX+it))
        }

        memory.add(SystemMessage(chatContext.agent.systemPrompt+"\n"+ AgentPromptTemplate.IGNORE_SENSITIVE_INFO_PROMPT))

        chatContext.messageHistory.forEach { messageEntity ->
            if (messageEntity.isUserMessage()) {
                memory.add(UserMessage(messageEntity.content))
            } else if (messageEntity.isAIMessage()) {
                memory.add(AiMessage(messageEntity.content))
            } else if (messageEntity.isSystemMessage()) {
                memory.add(SystemMessage(messageEntity.content))
            }
        }


    }

    protected open fun provideTools(): ToolProvider? {
        return null // 默认不提供工具
    }


    protected fun createLlmMessage(environment: ChatContext)= MessageEntity().apply {
        role=ASSISTANT
        sessionId=environment.sessionId
        model=environment.model.modelId
        provider=environment.provider.id
    }


    protected fun createUserMessage(environment: ChatContext) = MessageEntity().apply {
            role = USER
            content = environment.userMessage
            sessionId = environment.sessionId
    }


    /**
     * 错误处理辅助方法
     */
    protected fun <T>handleError(
        connection: T, transport: MessageTransport<T>,
        chatContext: ChatContext, message: String,
        llmEntity: MessageEntity, throwable: Throwable
    ) {

        val usedToken = OpenAiTokenCountEstimator(OpenAiChatModelName.GPT_4_O).run {
            estimateTokenCountInMessage(AiMessage.from(message))
        }


        llmEntity.apply {
            tokenCount=usedToken
            content=message
        }

        messageDomainService.saveMessageAndUpdateContext(
            listOf(llmEntity),
            chatContext.contextEntity
        )

        transport.sendEndMessage(connection, AgentChatResponse.buildEndMessage(
            throwable.message?:"", MessageType.TEXT)
        )

    }

}