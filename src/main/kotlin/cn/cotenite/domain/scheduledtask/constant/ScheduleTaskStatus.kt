package cn.cotenite.domain.scheduledtask.constant

import cn.cotenite.infrastructure.exception.BusinessException

/**
 * 定时任务状态枚举
 * @author  yhk
 * Description  
 * Date  2026/4/9 16:39
 */
enum class ScheduleTaskStatus {
    /** 活跃状态 */
    ACTIVE,

    /** 暂停状态 */
    PAUSED,

    /** 已完成状态 */
    COMPLETED;

    companion object {
        fun fromCode(code: String): ScheduleTaskStatus {
            return entries.find { it.name == code }
                ?: throw BusinessException("未知的任务状态码: $code")
        }
    }
}