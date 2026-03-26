package cn.cotenite.domain.shared.enums

/**
 * Token溢出策略
 */
enum class TokenOverflowStrategyEnum {
    NONE,
    SLIDING_WINDOW,
    SUMMARIZE;

    companion object {
        fun isValid(value: String?): Boolean {
            if (value.isNullOrBlank()) return false
            return entries.any { it.name == value }
        }

        fun fromString(value: String?): TokenOverflowStrategyEnum {
            if (value.isNullOrBlank()) return NONE
            return entries.firstOrNull { it.name == value } ?: NONE
        }
    }
}
