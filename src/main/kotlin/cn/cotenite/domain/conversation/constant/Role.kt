package cn.cotenite.domain.conversation.constant

/**
 * 对话角色
 */
enum class Role(
    val code: String
) {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    companion object {
        fun fromCode(code: String?): Role =
            entries.firstOrNull { it.code.equals(code, true) || it.name.equals(code, true) } ?: USER
    }
}
