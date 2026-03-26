package cn.cotenite.domain.agent.constant

/**
 * 发布状态
 */
enum class PublishStatus(
    /**
     * 状态编码
     */
    val code: Int,
    /**
     * 状态描述
     */
    val description: String
) {
    REVIEWING(1, "审核中"),
    PUBLISHED(2, "已发布"),
    REJECTED(3, "已拒绝"),
    REMOVED(4, "已下架");

    companion object {
        /**
         * 根据编码获取发布状态
         */
        fun fromCode(code: Int?): PublishStatus =
            entries.firstOrNull { it.code == code } ?: REVIEWING
    }
}
