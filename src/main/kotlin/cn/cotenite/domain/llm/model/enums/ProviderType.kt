package cn.cotenite.domain.llm.model.enums

/**
 * 服务商类型
 */
enum class ProviderType(
    val code: String
) {
    ALL("all"),
    OFFICIAL("official"),
    CUSTOM("custom");

    companion object {
        fun fromCode(code: String?): ProviderType =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
                ?: ALL
    }
}
