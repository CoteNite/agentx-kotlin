package cn.cotenite.agentxkotlin.domain.conversation.service


import cn.cotenite.agentxkotlin.domain.conversation.model.Context
import cn.cotenite.agentxkotlin.domain.conversation.model.Message
import cn.cotenite.agentxkotlin.domain.conversation.repository.ContextRepository
import cn.cotenite.agentxkotlin.domain.conversation.repository.MessageRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:32
 */
interface ContextService {

    /**
     * 获取会话上下文(活跃消息)
     */
    fun getContextMessages(sessionId: String): MutableList<Message>

    /**
     * 添加消息到上下文
     */
    fun addMessageToContext(sessionId: String, messageId: String)

    /**
     * 根据策略更新上下文
     * 当上下文消息过多时，应用管理策略
     */
    fun updateContext(sessionId: String)

    /**
     * 清空上下文
     */
    fun clearContext(sessionId: String)

    /**
     * 创建会话初始上下文
     */
    fun createInitialContext(sessionId: String)

    /**
     * 删除会话上下文
     */
    fun deleteContext(sessionId: String)

}

@Service
class ContextServiceImpl(
    private val contextRepository: ContextRepository,
    private val messageRepository: MessageRepository
) : ContextService {

    companion object{
        const val DEFAULT_CONTEXT_SIZE: Int = 10
    }

    override fun getContextMessages(sessionId: String): MutableList<Message> {
        val context = this.getOrCreateContext(sessionId)
        val messageIds: List<String> = context.getActiveMessageIds()

        if (messageIds.isEmpty()) {
            return mutableListOf()
        }

        val messages: MutableList<Message> = mutableListOf()
        messageIds.forEach { id ->
            val message = messageRepository.findByIdOrNull(id)
            if (message != null) {
                messages.add(message)
            }
        }

        return messages
    }

    private fun getOrCreateContext(sessionId: String): Context {
        val context: Context? = contextRepository.findBySessionIdAndDeletedAtIsNull(sessionId)

        if (context == null) {
            val newContext = Context.createNew(sessionId)
            contextRepository.save(newContext)
            return newContext
        }

        return context
    }

    @Transactional
    override fun addMessageToContext(sessionId: String, messageId: String) {
        val context = getOrCreateContext(sessionId)
        context.addMessage(messageId)
        context.updatedAt=LocalDateTime.now()
        contextRepository.save(context)

        updateContext(sessionId)
    }

    @Transactional
    override fun updateContext(sessionId: String) {
        val context = getOrCreateContext(sessionId)
        val activeIds = context.getActiveMessageIds()

        // 如果活跃消息数量超过限制，应用滑动窗口策略
        if (activeIds.size > DEFAULT_CONTEXT_SIZE) {
            // 保留最新的N条消息
            val newActiveIds = activeIds.subList(
                activeIds.size - DEFAULT_CONTEXT_SIZE,
                activeIds.size
            )
            context.setActiveMessageIds(newActiveIds)
            context.updatedAt=LocalDateTime.now()
            contextRepository.save(context)
        }
    }

    @Transactional
    override fun clearContext(sessionId: String) {
        val context = getOrCreateContext(sessionId)
        context.clear()
        contextRepository.save(context)
    }

    override fun createInitialContext(sessionId: String) {
        val existingContext = contextRepository.findBySessionIdAndDeletedAtIsNull(sessionId)

        if (existingContext == null) {
            val newContext = Context.createNew(sessionId)
            contextRepository.save(newContext)
        }
    }

    override fun deleteContext(sessionId: String) {
        contextRepository.softDeleteBySessionId(sessionId, java.time.LocalDateTime.now())
    }


}
