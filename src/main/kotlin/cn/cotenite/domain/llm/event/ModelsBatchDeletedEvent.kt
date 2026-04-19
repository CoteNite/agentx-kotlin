package cn.cotenite.domain.llm.event

/**
 * 模型批量删除事件
 *
 * @author xhy
 * @since 1.0.0
 */
class ModelsBatchDeletedEvent(
    /** 删除项列表 */
    val deleteItems: List<ModelDeleteItem>,
    /** 用户 ID */
    val userId: String
) {
    /** 模型删除项 */
    class ModelDeleteItem(
        val modelId: String,
        val userId: String
    )
}
