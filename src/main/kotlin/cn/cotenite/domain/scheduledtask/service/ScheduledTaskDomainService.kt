package cn.cotenite.domain.scheduledtask.service

import cn.cotenite.domain.scheduledtask.constant.ScheduleTaskStatus
import cn.cotenite.domain.scheduledtask.model.ScheduledTaskEntity
import cn.cotenite.domain.scheduledtask.repository.ScheduledTaskRepository
import cn.cotenite.infrastructure.exception.BusinessException
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/** 定时任务领域服务 处理定时任务相关的业务逻辑 */
@Service
class ScheduledTaskDomainService(
    private val scheduledTaskRepository: ScheduledTaskRepository
) {

    /** 创建定时任务 */
    fun createTask(task: ScheduledTaskEntity): ScheduledTaskEntity {
        scheduledTaskRepository.checkInsert(task)
        return task
    }

    /** 根据用户ID获取定时任务列表 */
    fun getTasksByUserId(userId: String): List<ScheduledTaskEntity> =
        scheduledTaskRepository.selectList(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::userId, userId)
                .orderByDesc(ScheduledTaskEntity::createdAt)
        )

    /** 根据用户ID和状态获取定时任务列表 */
    fun getTasksByUserIdAndStatus(userId: String, status: ScheduleTaskStatus): List<ScheduledTaskEntity> =
        scheduledTaskRepository.selectList(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::userId, userId)
                .eq(ScheduledTaskEntity::status, status)
                .orderByDesc(ScheduledTaskEntity::createdAt)
        )

    /** 根据会话ID获取定时任务列表 */
    fun getTasksBySessionId(sessionId: String): List<ScheduledTaskEntity> =
        scheduledTaskRepository.selectList(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::sessionId, sessionId)
                .orderByDesc(ScheduledTaskEntity::createdAt)
        )

    /** 根据 Agent ID 获取定时任务列表 */
    fun getTasksByAgentId(agentId: String): List<ScheduledTaskEntity> =
        scheduledTaskRepository.selectList(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::agentId, agentId)
                .orderByDesc(ScheduledTaskEntity::createdAt)
        )

    /** 获取需要执行的活跃任务 */
    fun getActiveTasksToExecute(): List<ScheduledTaskEntity> =
        scheduledTaskRepository.selectList(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::status, ScheduleTaskStatus.ACTIVE)
                .orderByAsc(ScheduledTaskEntity::createdAt)
        )

    /** 更新定时任务 */
    fun updateTask(task: ScheduledTaskEntity) {
        scheduledTaskRepository.checkedUpdate(
            task,
            KtUpdateWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::id, task.id)
                .eq(ScheduledTaskEntity::userId, task.userId)
        )
    }

    /** 删除定时任务 */
    fun deleteTask(taskId: String, userId: String) {
        scheduledTaskRepository.checkedDelete(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::id, taskId)
                .eq(ScheduledTaskEntity::userId, userId)
        )
    }

    /** 暂停定时任务 */
    fun pauseTask(taskId: String, userId: String) {
        scheduledTaskRepository.checkedUpdate(
            ScheduledTaskEntity().apply { status = ScheduleTaskStatus.PAUSED },
            KtUpdateWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::id, taskId)
                .eq(ScheduledTaskEntity::userId, userId)
        )
    }

    /** 恢复定时任务 */
    fun resumeTask(taskId: String, userId: String) {
        scheduledTaskRepository.checkedUpdate(
            ScheduledTaskEntity().apply { status = ScheduleTaskStatus.ACTIVE },
            KtUpdateWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::id, taskId)
                .eq(ScheduledTaskEntity::userId, userId)
        )
    }

    /** 完成定时任务 */
    fun completeTask(taskId: String, userId: String) {
        scheduledTaskRepository.checkedUpdate(
            ScheduledTaskEntity().apply { status = ScheduleTaskStatus.COMPLETED },
            KtUpdateWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::id, taskId)
                .eq(ScheduledTaskEntity::userId, userId)
        )
    }

    /** 记录任务执行时间 */
    fun recordExecution(taskId: String, executeTime: LocalDateTime) {
        scheduledTaskRepository.checkedUpdate(
            KtUpdateWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::id, taskId)
                .set(ScheduledTaskEntity::lastExecuteTime, executeTime)
        )
    }

    /** 检查任务是否存在 */
    fun checkTaskExist(taskId: String, userId: String) {
        scheduledTaskRepository.selectOne(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::id, taskId)
                .eq(ScheduledTaskEntity::userId, userId)
        ) ?: throw BusinessException("定时任务不存在")
    }

    /** 获取定时任务 */
    fun getTask(taskId: String, userId: String): ScheduledTaskEntity =
        scheduledTaskRepository.selectOne(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::id, taskId)
                .eq(ScheduledTaskEntity::userId, userId)
        ) ?: throw BusinessException("定时任务不存在")

    /** 统计用户的定时任务数量 */
    fun countByUserId(userId: String): Long =
        scheduledTaskRepository.selectCount(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::userId, userId)
        )

    /** 统计用户指定状态的定时任务数量 */
    fun countByUserIdAndStatus(userId: String, status: ScheduleTaskStatus): Long =
        scheduledTaskRepository.selectCount(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::userId, userId)
                .eq(ScheduledTaskEntity::status, status)
        )

    /** 批量删除指定会话的所有定时任务 */
    fun deleteTasksBySessionId(sessionId: String, userId: String): Int =
        scheduledTaskRepository.delete(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::sessionId, sessionId)
                .eq(ScheduledTaskEntity::userId, userId)
        )

    /** 批量删除指定 Agent 的所有定时任务 */
    fun deleteTasksByAgentId(agentId: String, userId: String): Int =
        scheduledTaskRepository.delete(
            KtQueryWrapper(ScheduledTaskEntity::class.java)
                .eq(ScheduledTaskEntity::agentId, agentId)
                .eq(ScheduledTaskEntity::userId, userId)
        )
}
