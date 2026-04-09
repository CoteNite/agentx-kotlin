package cn.cotenite.interfaces.api.portal.scheduledtask

import cn.cotenite.application.scheduledtask.dto.ScheduledTaskDTO
import cn.cotenite.application.scheduledtask.service.ScheduledTaskAppService
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.interfaces.api.common.Result
import cn.cotenite.interfaces.dto.scheduledtask.request.CreateScheduledTaskRequest
import cn.cotenite.interfaces.dto.scheduledtask.request.UpdateScheduledTaskRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/** 定时任务管理控制器 */
@RestController
@RequestMapping("/scheduled-task")
class PortalScheduledTaskController(
    private val scheduledTaskAppService: ScheduledTaskAppService
) {

    /** 创建定时任务 */
    @PostMapping
    fun createScheduledTask(@RequestBody @Validated request: CreateScheduledTaskRequest): Result<ScheduledTaskDTO> {
        val userId = UserContext.getCurrentUserId()
        return Result.success(scheduledTaskAppService.createScheduledTask(request, userId))
    }

    /** 更新定时任务 */
    @PutMapping("/{taskId}")
    fun updateScheduledTask(
        @PathVariable taskId: String,
        @RequestBody @Validated request: UpdateScheduledTaskRequest
    ): Result<ScheduledTaskDTO> {
        val userId = UserContext.getCurrentUserId()
        request.id = taskId
        return Result.success(scheduledTaskAppService.updateScheduledTask(request, userId))
    }

    /** 删除定时任务 */
    @DeleteMapping("/{taskId}")
    fun deleteScheduledTask(@PathVariable taskId: String): Result<Void> {
        scheduledTaskAppService.deleteTask(taskId, UserContext.getCurrentUserId())
        return Result.success()
    }

    /** 获取用户的定时任务列表（支持按 sessionId 或 agentId 过滤） */
    @GetMapping
    fun getScheduledTasks(
        @RequestParam(required = false) sessionId: String?,
        @RequestParam(required = false) agentId: String?
    ): Result<List<ScheduledTaskDTO>> {
        val userId = UserContext.getCurrentUserId()
        val tasks = when {
            !agentId.isNullOrBlank()   -> scheduledTaskAppService.getTasksByAgentId(agentId, userId)
            !sessionId.isNullOrBlank() -> scheduledTaskAppService.getTasksBySessionId(sessionId, userId)
            else                       -> scheduledTaskAppService.getUserTasks(userId)
        }
        return Result.success(tasks)
    }

    /** 根据 Agent ID 获取定时任务列表 */
    @GetMapping("/agent/{agentId}")
    fun getScheduledTasksByAgent(@PathVariable agentId: String): Result<List<ScheduledTaskDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Result.success(scheduledTaskAppService.getTasksByAgentId(agentId, userId))
    }

    /** 获取单个定时任务详情 */
    @GetMapping("/{taskId}")
    fun getScheduledTask(@PathVariable taskId: String): Result<ScheduledTaskDTO> {
        val userId = UserContext.getCurrentUserId()
        return Result.success(scheduledTaskAppService.getTask(taskId, userId))
    }

    /** 暂停定时任务 */
    @PostMapping("/{taskId}/pause")
    fun pauseTask(@PathVariable taskId: String): Result<ScheduledTaskDTO> {
        val userId = UserContext.getCurrentUserId()
        return Result.success(scheduledTaskAppService.pauseTask(taskId, userId))
    }

    /** 恢复定时任务 */
    @PostMapping("/{taskId}/resume")
    fun resumeTask(@PathVariable taskId: String): Result<ScheduledTaskDTO> {
        val userId = UserContext.getCurrentUserId()
        return Result.success(scheduledTaskAppService.resumeTask(taskId, userId))
    }
}
