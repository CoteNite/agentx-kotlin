package cn.cotenite.agentxkotlin.domain.conversation.service

import cn.cotenite.agentxkotlin.domain.conversation.model.ContextEntity
import cn.cotenite.agentxkotlin.domain.conversation.repository.ContextRepository
import cn.cotenite.agentxkotlin.domain.conversation.repository.MessageRepository
import org.springframework.stereotype.Service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 23:49
 */
@Service
class ContextDomainService(
    private val contextRepository: ContextRepository
){

    fun getBySessionId(sessionId: String): ContextEntity? {
        return contextRepository.findBySessionId(sessionId)
    }

    fun insertOrUpdate(contextEntity: ContextEntity): ContextEntity {
        return contextRepository.save(contextEntity)
    }
}