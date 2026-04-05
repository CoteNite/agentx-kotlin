package cn.cotenite.interfaces.api.portal.agent

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import cn.cotenite.application.task.service.TaskAppService
import cn.cotenite.domain.task.model.TaskAggregate
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.interfaces.api.common.Result

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskAppService: TaskAppService
) {

    @GetMapping("/session/{sessionId}/latest")
    fun getSessionTasks(@PathVariable sessionId: String): Result<TaskAggregate> =
        Result.success(taskAppService.getCurrentSessionTask(sessionId, UserContext.getCurrentUserId()))
}
