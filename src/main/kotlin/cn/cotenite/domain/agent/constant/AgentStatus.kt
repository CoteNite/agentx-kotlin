package cn.cotenite.domain.agent.constant

/**
 * Agent状态
 */
enum class AgentStatus(
    /**
     * 状态编码
     */
    val code: Int,
    /**
     * 状态描述
     */
    val description: String
) {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    companion object {
        /**
         * 根据编码获取状态
         */
        fun fromCode(code: Int?): AgentStatus =
            entries.firstOrNull { it.code == code } ?: DISABLED
    }
}
