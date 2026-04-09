package cn.cotenite.domain.scheduledtask.service

import cn.cotenite.domain.scheduledtask.constant.RepeatType
import cn.cotenite.domain.scheduledtask.model.RepeatConfig
import cn.cotenite.domain.scheduledtask.model.ScheduledTaskEntity
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** 任务调度服务 处理定时任务的调度逻辑 */
@Service
class TaskScheduleService {

    companion object {
        /** 时间匹配容差（分钟） */
        private const val TIME_TOLERANCE_MINUTES = 1L

        /** 一周的天数 */
        private const val DAYS_IN_WEEK = 7

        /** 时间单位常量 */
        private const val TIME_UNIT_DAYS = "DAYS"
        private const val TIME_UNIT_WEEKS = "WEEKS"
        private const val TIME_UNIT_MONTHS = "MONTHS"
    }

    /**
     * 计算任务的下次执行时间
     * @param task 定时任务
     * @param currentTime 当前时间
     * @return 下次执行时间，如果任务不需要再执行返回 null
     */
    fun calculateNextExecuteTime(task: ScheduledTaskEntity, currentTime: LocalDateTime): LocalDateTime? {
        val config = task.repeatConfig ?: return null

        return when (task.repeatType) {
            RepeatType.NONE -> {
                // 一次性任务，如果还没有执行过且执行时间在未来，返回执行时间
                val executeTime = config.executeDateTime
                if (executeTime != null && task.lastExecuteTime == null && executeTime.isAfter(currentTime)) {
                    executeTime
                } else null
            }
            RepeatType.DAILY    -> calculateDailyNextTime(config, currentTime)
            RepeatType.WEEKLY   -> calculateWeeklyNextTime(config, currentTime)
            RepeatType.MONTHLY  -> calculateMonthlyNextTime(config, currentTime)
            RepeatType.WORKDAYS -> calculateWorkdaysNextTime(config, currentTime)
            RepeatType.CUSTOM   -> calculateCustomNextTime(config, currentTime)
            else                -> null
        }
    }

    /**
     * 检查任务是否应该在指定时间执行
     * @param task 定时任务
     * @param checkTime 检查时间
     * @return 是否应该执行
     */
    fun shouldExecuteAt(task: ScheduledTaskEntity, checkTime: LocalDateTime): Boolean {
        val config = task.repeatConfig ?: return false
        val executeTime = config.executeDateTime ?: return false

        // 检查是否已经到了执行时间
        if (checkTime.isBefore(executeTime)) return false

        // 检查上次执行时间，避免重复执行
        val lastExecuteTime = task.lastExecuteTime
        if (lastExecuteTime != null && !checkTime.isAfter(lastExecuteTime.plusMinutes(TIME_TOLERANCE_MINUTES))) {
            return false
        }

        return when (task.repeatType) {
            RepeatType.NONE -> {
                // 一次性任务，只在首次执行时间执行
                lastExecuteTime == null
                        && checkTime.isAfter(executeTime.minusMinutes(TIME_TOLERANCE_MINUTES))
                        && checkTime.isBefore(executeTime.plusMinutes(TIME_TOLERANCE_MINUTES))
            }
            RepeatType.DAILY    -> shouldExecuteDaily(config, checkTime)
            RepeatType.WEEKLY   -> shouldExecuteWeekly(config, checkTime)
            RepeatType.MONTHLY  -> shouldExecuteMonthly(config, checkTime)
            RepeatType.WORKDAYS -> shouldExecuteWorkdays(config, checkTime)
            RepeatType.CUSTOM   -> shouldExecuteCustom(config, checkTime)
            else                -> false
        }
    }

    // ── 计算下次执行时间 ──────────────────────────────────────────────────────

    private fun calculateDailyNextTime(config: RepeatConfig, currentTime: LocalDateTime): LocalDateTime {
        val time: LocalTime = config.executeDateTime!!.toLocalTime()
        var nextTime = currentTime.toLocalDate().atTime(time)
        if (!nextTime.isAfter(currentTime)) {
            nextTime = nextTime.plusDays(1)
        }
        return nextTime
    }

    private fun calculateWeeklyNextTime(config: RepeatConfig, currentTime: LocalDateTime): LocalDateTime? {
        val weekdays = config.weekdays?.takeIf { it.isNotEmpty() } ?: return null
        val time: LocalTime = config.executeDateTime!!.toLocalTime()
        val base = currentTime.toLocalDate().atTime(time)

        for (i in 0 until DAYS_IN_WEEK) {
            val candidate = base.plusDays(i.toLong())
            if (weekdays.contains(candidate.dayOfWeek.value) && candidate.isAfter(currentTime)) {
                return candidate
            }
        }
        return null
    }

    private fun calculateMonthlyNextTime(config: RepeatConfig, currentTime: LocalDateTime): LocalDateTime? {
        val monthDay = config.monthDay ?: return null
        val time: LocalTime = config.executeDateTime!!.toLocalTime()
        var nextTime = currentTime.toLocalDate().withDayOfMonth(monthDay).atTime(time)
        if (!nextTime.isAfter(currentTime)) {
            nextTime = nextTime.plusMonths(1)
        }
        return nextTime
    }

    private fun calculateWorkdaysNextTime(config: RepeatConfig, currentTime: LocalDateTime): LocalDateTime {
        val time: LocalTime = config.executeDateTime!!.toLocalTime()
        var nextTime = currentTime.toLocalDate().atTime(time)
        while (true) {
            if (nextTime.isAfter(currentTime) && isWorkday(nextTime)) return nextTime
            nextTime = nextTime.plusDays(1)
        }
    }

    private fun calculateCustomNextTime(config: RepeatConfig, currentTime: LocalDateTime): LocalDateTime? {
        val interval = config.interval ?: return null
        val timeUnit = config.timeUnit ?: return null
        val endDateTime = config.endDateTime

        if (endDateTime != null && currentTime.isAfter(endDateTime)) return null

        var nextTime = config.executeDateTime!!

        while (!nextTime.isAfter(currentTime)) {
            nextTime = when (timeUnit.uppercase()) {
                TIME_UNIT_DAYS   -> nextTime.plusDays(interval.toLong())
                TIME_UNIT_WEEKS  -> nextTime.plusWeeks(interval.toLong())
                TIME_UNIT_MONTHS -> nextTime.plusMonths(interval.toLong())
                else             -> return null
            }
        }

        return if (endDateTime != null && nextTime.isAfter(endDateTime)) null else nextTime
    }

    // ── 检查是否应该执行 ──────────────────────────────────────────────────────

    private fun shouldExecuteDaily(config: RepeatConfig, checkTime: LocalDateTime): Boolean {
        val executeTime: LocalTime = config.executeDateTime!!.toLocalTime()
        return Math.abs(ChronoUnit.MINUTES.between(executeTime, checkTime.toLocalTime())) <= TIME_TOLERANCE_MINUTES
    }

    private fun shouldExecuteWeekly(config: RepeatConfig, checkTime: LocalDateTime): Boolean {
        val weekdays = config.weekdays?.takeIf { it.isNotEmpty() } ?: return false
        if (!weekdays.contains(checkTime.dayOfWeek.value)) return false
        return shouldExecuteDaily(config, checkTime)
    }

    private fun shouldExecuteMonthly(config: RepeatConfig, checkTime: LocalDateTime): Boolean {
        val monthDay = config.monthDay ?: return false
        if (checkTime.dayOfMonth != monthDay) return false
        return shouldExecuteDaily(config, checkTime)
    }

    private fun shouldExecuteWorkdays(config: RepeatConfig, checkTime: LocalDateTime): Boolean {
        if (!isWorkday(checkTime)) return false
        return shouldExecuteDaily(config, checkTime)
    }

    private fun shouldExecuteCustom(config: RepeatConfig, checkTime: LocalDateTime): Boolean {
        val interval = config.interval ?: return false
        val timeUnit = config.timeUnit ?: return false
        val endDateTime = config.endDateTime

        if (endDateTime != null && checkTime.isAfter(endDateTime)) return false

        val executeTime = config.executeDateTime!!

        val totalUnits: Long = when (timeUnit.uppercase()) {
            TIME_UNIT_DAYS   -> ChronoUnit.DAYS.between(executeTime.toLocalDate(), checkTime.toLocalDate())
            TIME_UNIT_WEEKS  -> ChronoUnit.WEEKS.between(executeTime.toLocalDate(), checkTime.toLocalDate())
            TIME_UNIT_MONTHS -> ChronoUnit.MONTHS.between(executeTime.toLocalDate(), checkTime.toLocalDate())
            else             -> return false
        }

        if (totalUnits % interval != 0L) return false
        return shouldExecuteDaily(config, checkTime)
    }

    // ── 工具方法 ──────────────────────────────────────────────────────────────

    private fun isWorkday(dateTime: LocalDateTime): Boolean =
        dateTime.dayOfWeek != DayOfWeek.SATURDAY && dateTime.dayOfWeek != DayOfWeek.SUNDAY
}
