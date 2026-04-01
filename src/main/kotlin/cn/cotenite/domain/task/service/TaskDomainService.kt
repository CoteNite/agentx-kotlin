package cn.cotenite.domain.task.service

import cn.cotenite.domain.task.model.TaskAggregate
import cn.cotenite.domain.task.model.TaskEntity
import cn.cotenite.domain.task.repository.TaskRepository
import cn.cotenite.infrastructure.exception.BusinessException
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * @author  yhk
 * Description  
 * Date  2026/4/1 18:39
 */
@Service
class TaskDomainService(
    private val taskRepository: TaskRepository
){

    fun addTask(taskEntity: TaskEntity): TaskEntity{
        taskRepository.checkInsert(taskEntity.apply {
            startTime= LocalDateTime.now()
        })
        return taskEntity
    }

    fun updateTask(taskEntity: TaskEntity): TaskEntity{
        taskRepository.checkedUpdateById(taskEntity)
        return taskEntity
    }


    fun getCurrentSessionTask(sessionId: String,userId: String): TaskAggregate{
        val taskEntity = taskRepository.selectOne(
            KtQueryWrapper(TaskEntity::class.java)
                .eq(TaskEntity::sessionId, sessionId)
                .eq(TaskEntity::userId, userId)
                .eq(TaskEntity::parentTaskId, "0")
                .orderByDesc(TaskEntity::createdAt)
                .last("limit 1")
        )

        val subTasks = getSubTasks(taskEntity.id?:throw BusinessException("未找到当前会话的任务"))

        return TaskAggregate(taskEntity,subTasks)
    }

    /**
     * 根据父任务id查出子任务
     * @param parentTaskId 父任务id
     * @return
     */
    fun getSubTasks(parentTaskId: String): List<TaskEntity> {
        return taskRepository.selectList(
            KtQueryWrapper(TaskEntity::class.java)
                .eq(TaskEntity::parentTaskId,parentTaskId)
        )
    }
}

