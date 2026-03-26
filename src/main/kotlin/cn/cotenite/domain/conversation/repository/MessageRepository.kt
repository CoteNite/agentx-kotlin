package cn.cotenite.domain.conversation.repository

import org.apache.ibatis.annotations.Mapper
import cn.cotenite.domain.conversation.model.MessageEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * 消息仓储接口
 */
@Mapper
interface MessageRepository : MyBatisPlusExtRepository<MessageEntity>
