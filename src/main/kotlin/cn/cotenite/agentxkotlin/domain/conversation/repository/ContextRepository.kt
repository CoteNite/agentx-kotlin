package cn.cotenite.agentxkotlin.domain.conversation.repository

import cn.cotenite.agentxkotlin.domain.conversation.model.Context
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description Context JPA Repository
 * @Date  2025/6/16 17:29
 */
@Repository
interface ContextRepository : JpaRepository<Context, String> {

    // 根据会话ID查找上下文
    fun findBySessionIdAndDeletedAtIsNull(sessionId: String): Context?

    // 根据会话ID软删除所有上下文
    @Modifying
    @Query("UPDATE Context c SET c.deletedAt = :deletedAt WHERE c.sessionId = :sessionId")
    fun softDeleteBySessionId(@Param("sessionId") sessionId: String, @Param("deletedAt") deletedAt: LocalDateTime): Int
}
