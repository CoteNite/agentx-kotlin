package cn.cotenite.application.scheduledtask.service

import cn.cotenite.application.scheduledtask.assembler.ScheduledTaskAssembler
import cn.cotenite.application.scheduledtask.dto.ScheduledTaskDTO
import cn.cotenite.domain.scheduledtask.service.ScheduledTaskDomainService
import cn.cotenite.domain.scheduledtask.service.ScheduledTaskExecutionService
import cn.cotenite.domain.scheduledtask.service.TaskScheduleService
import cn.cotenite.interfaces.dto.scheduledtask.request.CreateScheduledTaskRequest
import cn.cotenite.interfaces.dto.scheduledtask.request.UpdateScheduledTaskRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/** 定时任务应用服务 */
@Service
class ScheduledTaskAppService(
    private val scheduledTaskDomainService: ScheduledTaskDomainService,
    private val taskScheduleService: TaskScheduleService,
    private val executionService: ScheduledTaskExecutionService
) {

    /** 创建定时任务 */
    @Transactional
    fun createScheduledTask(request: CreateScheduledTaskRequest, userId: String): ScheduledTaskDTO {
        val entity = ScheduledTaskAssembler.toEntity(request, userId).apply {
            nextExecuteTime = taskScheduleService.calculateNextExecuteTime(this, LocalDateTime.now())
        }
        val savedEntity = scheduledTaskDomainService.createTask(entity)
        executionService.scheduleTask(savedEntity)
        return ScheduledTaskAssembler.toDTO(savedEntity)!!
    }

    /** 更新定时任务 */
    @Transactional
    fun updateScheduledTask(request: UpdateScheduledTaskRequest, userId: String): ScheduledTaskDTO {
        val updateEntity = ScheduledTaskAssembler.toEntity(request, userId)



        if (updateEntity.repeatConfig != null) {
            updateEntity.nextExecuteTime = taskScheduleService.calculateNextExecuteTime(updateEntity, LocalDateTime.now())
        }

        scheduledTaskDomainService.updateTask(updateEntity)
        executionService.rescheduleTask(updateEntity)

        return ScheduledTaskAssembler.toDTO(updateEntity)!!
    }

    /** 删除定时任务 */
    @Transactional
    fun deleteTask(taskId: String, userId: String) {
        executionService.deleteTask(taskId, userId)
    }

    /** 获取单个定时任务 */
    fun getTask(taskId: String, userId: String): ScheduledTaskDTO =
        ScheduledTaskAssembler.toDTO(scheduledTaskDomainService.getTask(taskId, userId))!!

    /** 获取用户的定时任务列表 */
    fun getUserTasks(userId: String): List<ScheduledTaskDTO> =
        scheduledTaskDomainService.getTasksByUserId(userId).mapNotNull { ScheduledTaskAssembler.toDTO(it) }

    /** 根据会话ID获取定时任务列表 */
    fun getTasksBySessionId(sessionId: String, userId: String): List<ScheduledTaskDTO> =
        scheduledTaskDomainService.getTasksBySessionId(sessionId)
            .filter { it.userId == userId }
            .mapNotNull { ScheduledTaskAssembler.toDTO(it) }

    /** 根据Agent ID获取定时任务列表 */
    fun getTasksByAgentId(agentId: String, userId: String): List<ScheduledTaskDTO> =
        scheduledTaskDomainService.getTasksByAgentId(agentId)
            .filter { it.userId == userId }
            .mapNotNull { ScheduledTaskAssembler.toDTO(it) }

    /** 暂停定时任务 */
    @Transactional
    fun pauseTask(taskId: String, userId: String): ScheduledTaskDTO {
        executionService.pauseTask(taskId, userId)
        return ScheduledTaskAssembler.toDTO(scheduledTaskDomainService.getTask(taskId, userId))!!
    }

    /** 恢复定时任务 */
    @Transactional
    fun resumeTask(taskId: String, userId: String): ScheduledTaskDTO {
        executionService.resumeTask(taskId, userId)
        return ScheduledTaskAssembler.toDTO(scheduledTaskDomainService.getTask(taskId, userId))!!
    }
}
