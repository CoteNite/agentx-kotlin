package cn.cotenite.domain.llm.event

import cn.cotenite.domain.llm.model.ModelEntity

/**
 * 模型状态变更事件，当模型状态发生变更时触发
 *
 * @author xhy
 * @since 1.0.0
 */
class ModelStatusChangedEvent(
    modelId: String,
    userId: String,
    /** 变更后的模型实体 */
    val model: ModelEntity,
    /** 新状态，true=启用，false=禁用 */
    val enabled: Boolean,
    /** 状态变更原因 */
    val reason: String? = null
) : ModelDomainEvent(modelId, userId) {
    /** 是否为启用事件 */
    fun isActivation(): Boolean = enabled

    /** 是否为禁用事件 */
    fun isDeactivation(): Boolean = !enabled
}
