package cn.cotenite.domain.llm.repository

import org.apache.ibatis.annotations.Mapper
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * 模型仓储接口
 */
@Mapper
interface ModelRepository : MyBatisPlusExtRepository<ModelEntity>
