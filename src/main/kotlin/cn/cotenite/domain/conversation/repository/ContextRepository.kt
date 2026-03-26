package cn.cotenite.domain.conversation.repository

import org.apache.ibatis.annotations.Mapper
import cn.cotenite.domain.conversation.model.ContextEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * 上下文仓储接口
 */
@Mapper
interface ContextRepository : MyBatisPlusExtRepository<ContextEntity>
