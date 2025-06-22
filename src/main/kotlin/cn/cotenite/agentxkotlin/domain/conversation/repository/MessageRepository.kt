package cn.cotenite.agentxkotlin.domain.conversation.repository

import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description Message JPA Repository
 * @Date  2025/6/16 17:29
 */
@Repository
interface MessageRepository : JpaRepository<MessageEntity, String> {

    /**
     * 根据会话ID查找未删除的消息，按创建时间升序排列
     */
    fun findBySessionIdAndDeletedAtIsNullOrderByCreatedAtAsc(sessionId: String): List<MessageEntity>

    /**
     * 根据会话ID查找未删除的消息
     */
    fun findBySessionIdAndDeletedAtIsNull(sessionId: String): List<MessageEntity>

    /**
     * 根据会话ID列表查找未删除的消息
     */
    fun findBySessionIdInAndDeletedAtIsNull(sessionIds: List<String>): List<MessageEntity>

    /**
     * 软删除指定会话的所有消息
     */
    @Modifying
    @Query("UPDATE MessageEntity m SET m.deletedAt = CURRENT_TIMESTAMP WHERE m.sessionId = :sessionId AND m.deletedAt IS NULL")
    fun softDeleteBySessionId(@Param("sessionId") sessionId: String)

    /**
     * 批量软删除指定会话列表的所有消息
     */
    @Modifying
    @Query("UPDATE MessageEntity m SET m.deletedAt = CURRENT_TIMESTAMP WHERE m.sessionId IN :sessionIds AND m.deletedAt IS NULL")
    fun softDeleteBySessionIdIn(@Param("sessionIds") sessionIds: List<String>)
}
