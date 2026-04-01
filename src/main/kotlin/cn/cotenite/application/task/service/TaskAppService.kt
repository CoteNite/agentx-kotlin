package cn.cotenite.application.task.service

import cn.cotenite.domain.task.model.TaskAggregate
import cn.cotenite.domain.task.service.TaskDomainService
import org.springframework.stereotype.Service

/**
 * @author  yhk
 * Description  
 * Date  2026/4/1 18:59
 */
@Service
class TaskAppService(
    private val taskDomainService: TaskDomainService
){

    fun getCurrentSessionTask(sessionId: String,userId: String): TaskAggregate{
        return taskDomainService.getCurrentSessionTask(sessionId,userId)
    }

}