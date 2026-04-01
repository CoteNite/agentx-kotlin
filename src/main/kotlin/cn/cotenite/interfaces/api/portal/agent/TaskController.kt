package cn.cotenite.interfaces.api.portal.agent

import cn.cotenite.application.task.service.TaskAppService
import cn.cotenite.domain.task.model.TaskAggregate
import cn.cotenite.infrastructure.auth.UserContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping

/**
 * @author  yhk
 * Description  
 * Date  2026/4/1 21:43
 */
@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskAppService: TaskAppService
){

    @GetMapping("/session/{sessionId}/latest")
    fun getSessionTasks(@PathVariable sessionId: String): Result<TaskAggregate>{
        val userId = UserContext.getCurrentUserId()
        return Result.success(taskAppService.getCurrentSessionTask(sessionId,userId))
    }

}
