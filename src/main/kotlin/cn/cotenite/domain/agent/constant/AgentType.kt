package cn.cotenite.domain.agent.constant

/**
 * Agent类型
 */
enum class AgentType(
    /**
     * 类型编码
     */
    val code: Int,
    /**
     * 类型描述
     */
    val description: String
) {
    CHAT_ASSISTANT(1, "聊天助手"),
    FUNCTIONAL_AGENT(2, "功能型助理");

    companion object {
        /**
         * 根据编码获取类型
         */
        fun fromCode(code: Int?): AgentType =
            entries.firstOrNull { it.code == code } ?: CHAT_ASSISTANT
    }
}
