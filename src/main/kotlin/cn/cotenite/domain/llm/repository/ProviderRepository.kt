package cn.cotenite.domain.llm.repository

import org.apache.ibatis.annotations.Mapper
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * 服务提供商仓储接口
 */
@Mapper
interface ProviderRepository : MyBatisPlusExtRepository<ProviderEntity>
