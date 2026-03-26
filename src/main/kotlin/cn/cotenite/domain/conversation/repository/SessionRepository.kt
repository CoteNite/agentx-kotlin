package cn.cotenite.domain.conversation.repository

import org.apache.ibatis.annotations.Mapper
import cn.cotenite.domain.conversation.model.SessionEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * 会话仓储接口
 */
@Mapper
interface SessionRepository : MyBatisPlusExtRepository<SessionEntity>
