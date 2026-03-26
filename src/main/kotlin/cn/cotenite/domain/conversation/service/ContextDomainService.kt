package cn.cotenite.domain.conversation.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import org.springframework.stereotype.Service
import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.domain.conversation.repository.ContextRepository
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * 上下文领域服务
 */
@Service
class ContextDomainService(
    private val contextRepository: ContextRepository
) {

    fun getBySessionId(sessionId: String): ContextEntity =
        findBySessionId(sessionId) ?: throw BusinessException("消息上下文不存在")

    fun findBySessionId(sessionId: String): ContextEntity? =
        contextRepository.selectOne(
            KtQueryWrapper(ContextEntity::class.java)
                .eq(ContextEntity::sessionId, sessionId)
        )

    fun insertOrUpdate(contextEntity: ContextEntity): ContextEntity =
        contextEntity.apply { contextRepository.insertOrUpdate(this) }
}
