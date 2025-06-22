package cn.cotenite.agentxkotlin.domain.conversation.repository

import cn.cotenite.agentxkotlin.domain.conversation.model.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
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
interface MessageRepository : JpaRepository<MessageEntity, String>, JpaSpecificationExecutor<MessageEntity> {

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

    @Modifying // 标记这是一个修改（删除/更新）操作
    @Query("delete from MessageEntity m where m.sessionId = :sessionId")
    fun deleteBySessionId(@Param("sessionId") sessionId: String) // 使用 @Param 绑定参数

    @Modifying
    @Query("delete from MessageEntity m where m.sessionId in :sessionIds")
    fun deleteBySessionIds(@Param("sessionIds") sessionIds: List<String>)

}
