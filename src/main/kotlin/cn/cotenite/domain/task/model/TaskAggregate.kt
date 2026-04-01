package cn.cotenite.domain.task.model

/**
 * @author  yhk
 * Description  
 * Date  2026/4/1 18:32
 */
class TaskAggregate(
    private val task: TaskEntity,
    private val subTasks:List<TaskEntity>
){


    fun getTask(): TaskEntity {
        return task
    }

    fun getSubTasks(): List<TaskEntity> {
        return subTasks
    }

}