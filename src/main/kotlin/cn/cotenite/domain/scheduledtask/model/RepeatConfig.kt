package cn.cotenite.domain.scheduledtask.model

import java.time.LocalDateTime

/**
 * 重复配置值对象
 * 根据不同的重复类型存储相应的配置信息
 *
 * @author yhk
 * @since 2026/4/9 16:40
 */
class RepeatConfig(
    /** 执行时间 */
    var executeDateTime: LocalDateTime? = null,

    /** 每周重复时的星期几列表 (1-7, 1表示周一) */
    var weekdays: List<Int>? = null,

    /** 每月重复时的日期 (1-31) */
    var monthDay: Int? = null,

    /** 自定义重复的间隔数 */
    var interval: Int? = null,

    /** 自定义重复的时间单位 (DAYS, WEEKS, MONTHS) */
    var timeUnit: String? = null,

    /** 自定义重复的执行时间 */
    var executeTime: String? = null,

    /** 自定义重复的截止日期 */
    var endDateTime: LocalDateTime? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RepeatConfig

        if (executeDateTime != other.executeDateTime) return false
        if (weekdays != other.weekdays) return false
        if (monthDay != other.monthDay) return false
        if (interval != other.interval) return false
        if (timeUnit != other.timeUnit) return false
        if (executeTime != other.executeTime) return false
        if (endDateTime != other.endDateTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = executeDateTime?.hashCode() ?: 0
        result = 31 * result + (weekdays?.hashCode() ?: 0)
        result = 31 * result + (monthDay ?: 0)
        result = 31 * result + (interval ?: 0)
        result = 31 * result + (timeUnit?.hashCode() ?: 0)
        result = 31 * result + (executeTime?.hashCode() ?: 0)
        result = 31 * result + (endDateTime?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "RepeatConfig(executeDateTime=$executeDateTime, weekdays=$weekdays, monthDay=$monthDay, interval=$interval, timeUnit='$timeUnit', executeTime='$executeTime', endDateTime=$endDateTime)"
    }
}