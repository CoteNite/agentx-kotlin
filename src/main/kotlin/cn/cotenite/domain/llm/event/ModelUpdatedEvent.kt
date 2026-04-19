package cn.cotenite.domain.llm.event

import cn.cotenite.domain.llm.model.ModelEntity

/**
 * 模型更新事件
 *
 * @author xhy
 * @since 1.0.0
 */
class ModelUpdatedEvent(
    modelId: String,
    userId: String,
    /** 更新后的模型实体 */
    val model: ModelEntity
) : ModelDomainEvent(modelId, userId)
