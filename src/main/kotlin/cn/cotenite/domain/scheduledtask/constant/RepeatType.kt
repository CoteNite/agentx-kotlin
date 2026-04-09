package cn.cotenite.domain.scheduledtask.constant

import cn.cotenite.infrastructure.exception.BusinessException

/**
 * 定时任务重复类型枚举
 * @author  yhk
 * Description  
 * Date  2026/4/9 16:38
 */
enum class RepeatType {
    /** 不重复 */
    NONE,

    /** 每天 */
    DAILY,

    /** 每周 */
    WEEKLY,

    /** 每月 */
    MONTHLY,

    /** 工作日 */
    WORKDAYS,

    /** 自定义 */
    CUSTOM;

    companion object {
        fun fromCode(code: String): RepeatType {
            return entries.find { it.name == code }
                ?: throw BusinessException("未知的重复类型码: $code")
        }
    }
}