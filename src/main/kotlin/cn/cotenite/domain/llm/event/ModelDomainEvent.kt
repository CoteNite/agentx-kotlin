package cn.cotenite.domain.llm.event

import java.time.LocalDateTime

/**
 * 模型领域事件基类
 *
 * @author xhy
 * @since 1.0.0
 */
abstract class ModelDomainEvent(
    /** 模型 ID */
    val modelId: String,
    /** 用户 ID */
    val userId: String
) {
    /** 事件发生时间 */
    val occurredAt: LocalDateTime = LocalDateTime.now()
}
