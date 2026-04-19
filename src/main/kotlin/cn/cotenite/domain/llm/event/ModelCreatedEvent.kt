package cn.cotenite.domain.llm.event

import cn.cotenite.domain.llm.model.ModelEntity

/**
 * 模型创建事件
 *
 * @author xhy
 * @since 1.0.0
 */
class ModelCreatedEvent(
    modelId: String,
    userId: String,
    /** 模型实体 */
    val model: ModelEntity
) : ModelDomainEvent(modelId, userId)
