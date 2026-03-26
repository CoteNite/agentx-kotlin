package cn.cotenite.infrastructure.transport

import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import cn.cotenite.application.conversation.dto.StreamChatResponse
import cn.cotenite.domain.conversation.handler.ChatEnvironment
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.domain.conversation.service.ContextDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService

/**
 * SSE消息传输实现 - 使用 coroutines 消费 Flow
 */
@Component
class SseMessageTransport(
    private val conversationDomainService: ConversationDomainService,
    private val contextDomainService: ContextDomainService
) : MessageTransport<SseEmitter> {

    override fun createConnection(timeout: Long): SseEmitter =
        SseEmitter(timeout).apply {
            onTimeout {
                runCatching {
                    send(StreamChatResponse(content = "\n\n[系统提示：响应超时，请重试]", done = true))
                }
                complete()
            }
            onError { ex ->
                runCatching {
                    send(StreamChatResponse(content = "\n\n[系统错误：${ex.message}]", done = true))
                }
                complete()
            }
        }

    override fun sendMessage(
        connection: SseEmitter,
        content: String,
        isDone: Boolean,
        provider: String?,
        model: String?
    ) = connection.send(StreamChatResponse(content = content, done = isDone, provider = provider, model = model))

    override fun completeConnection(connection: SseEmitter) = connection.complete()

    override fun handleError(connection: SseEmitter, error: Throwable) {
        runCatching { connection.send(StreamChatResponse(content = error.message, done = true)) }
        connection.complete()
    }

    override fun handleStreamingResponse(
        connection: SseEmitter,
        flow: Flow<StreamingChatModelReply>,
        environment: ChatEnvironment,
        userMessageEntity: MessageEntity,
        llmMessageEntity: MessageEntity
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                flow.collect { reply ->
                    when (reply) {
                        is StreamingChatModelReply.PartialResponse -> {
                            runCatching {
                                sendMessage(
                                    connection,
                                    reply.partialResponse,
                                    false,
                                    environment.provider.name,
                                    environment.model.modelId
                                )
                            }
                        }
                        is StreamingChatModelReply.CompleteResponse -> {
                            val tokenUsage = reply.response.metadata().tokenUsage()
                            userMessageEntity.tokenCount = tokenUsage.inputTokenCount() ?: 0
                            llmMessageEntity.tokenCount = tokenUsage.outputTokenCount() ?: 0
                            llmMessageEntity.content = reply.response.aiMessage().text()

                            runCatching {
                                sendMessage(connection, "", true, environment.provider.name, environment.model.modelId)
                                completeConnection(connection)
                            }

                            // 持久化消息
                            conversationDomainService.insertBathMessage(listOf(userMessageEntity, llmMessageEntity))

                            // 更新上下文
                            environment.contextEntity.activeMessages.add(userMessageEntity.id.orEmpty())
                            environment.contextEntity.activeMessages.add(llmMessageEntity.id.orEmpty())
                            contextDomainService.insertOrUpdate(environment.contextEntity)
                        }
                        is StreamingChatModelReply.Error -> handleError(connection, reply.cause)
                    }
                }
            } catch (e: Exception) {
                handleError(connection, e)
            }
        }
    }
}
