package cn.cotenite.agentxkotlin.domain.sahred.enums

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/23 00:52
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

       /*** 判断给定字符串是否为有效的枚举值
        *
        * @param value 策略名称字符串
        * @return 是否为有效的策略枚举
        */
       fun isValid(value: String): Boolean{
           try {
               TokenOverflowStrategyEnum.valueOf(value)
               return true
           } catch (e:IllegalArgumentException) {
               return false
           }
       }

       /**
        * 从字符串转换为枚举值，如果不存在则返回默认值NONE
        *
        * @param value 策略名称字符串
        * @return 对应的策略枚举值，如果不匹配则返回NONE
        */
        fun fromString(value: String):TokenOverflowStrategyEnum{
            return try {
                TokenOverflowStrategyEnum.valueOf(value)
            }catch (e: IllegalArgumentException) {
                NONE
           }
        }
   }

}