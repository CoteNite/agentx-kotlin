package cn.cotenite.agentxkotlin.domain.token.model.enums

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/20 22:44
 */
enum class TokenOverflowStrategyEnum {

    /**
     * 无策略 - 不做任何处理，可能导致超限错误
     */
    NONE,

    /**
     * 滑动窗口 - 自动移除旧消息，保留最新内容
     */
    SLIDING_WINDOW,

    /**
     * 摘要策略 - 将旧消息转换为摘要，保留关键信息
     */
    SUMMARIZE;

    companion object{
        fun isValid(strategy: String): Boolean {
            try {
                TokenOverflowStrategyEnum.valueOf(strategy)
                return true
            }catch (_: IllegalStateException){
                return false
            }
        }

        fun fromString(strategy: String): TokenOverflowStrategyEnum {
            return try {
                TokenOverflowStrategyEnum.valueOf(strategy)
            }catch (_:IllegalStateException){
                NONE
            }
        }
    }
}