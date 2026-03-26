package cn.cotenite.domain.llm.model.enums

/**
 * 模型类型
 */
enum class ModelType(
    val code: String
) {
    CHAT("chat"),
    EMBEDDING("embedding"),
    RERANK("rerank"),
    OTHER("other");

    companion object {
        fun fromCode(code: String?): ModelType =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
                ?: CHAT
    }
}
