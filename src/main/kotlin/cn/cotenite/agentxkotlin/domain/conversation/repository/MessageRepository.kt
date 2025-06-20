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

    // 根据会话ID查找消息
    fun findBySessionIdAndDeletedAtIsNullOrderByCreatedAtAsc(sessionId: String): List<MessageEntity>

    // 软删除
    @Modifying
    @Query("UPDATE MessageEntity m SET m.deletedAt = :deletedAt WHERE m.id = :id")
    fun softDeleteById(@Param("id") id: String, @Param("deletedAt") deletedAt: LocalDateTime): Int

    // 根据会话ID软删除所有消息
    @Modifying
    @Query("UPDATE MessageEntity m SET m.deletedAt = :deletedAt WHERE m.sessionId = :sessionId")
    fun softDeleteBySessionId(@Param("sessionId") sessionId: String, @Param("deletedAt") deletedAt: LocalDateTime): Int

    fun deleteBySessionIdIn(sessionIds: List<String>) : Int
}
