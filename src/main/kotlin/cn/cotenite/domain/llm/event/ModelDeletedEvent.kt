package cn.cotenite.domain.llm.event

/**
 * 模型删除事件
 *
 * @author xhy
 * @since 1.0.0
 */
class ModelDeletedEvent(
    modelId: String,
    userId: String
) : ModelDomainEvent(modelId, userId)
