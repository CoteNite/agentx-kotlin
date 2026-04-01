package cn.cotenite.application.task.assembler

import cn.cotenite.application.task.dto.TaskDTO
import cn.cotenite.domain.task.model.TaskEntity
import kotlin.collections.mapNotNull

/**
 * @author  yhk
 * Description  
 * Date  2026/4/1 18:23
 */
object TaskAssembler {

    /**
     * 将实体转换为DTO
     * 使用 apply 函数可以优雅地初始化对象
     */
    fun toDTO(entity: TaskEntity): TaskDTO {
        return entity.run {
            TaskDTO().apply {
                id = this@run.id
                sessionId = this@run.sessionId
                userId = this@run.userId
                parentTaskId = this@run.parentTaskId
                taskName = this@run.taskName
                description = this@run.description
                status = this@run.status?.name
                progress = this@run.progress
                startTime = this@run.startTime
                endTime = this@run.endTime
                createdAt = this@run.createdAt
                updatedAt = this@run.updatedAt
            }
        }
    }

    /**
     * 将实体列表转换为DTO列表
     * 使用空安全操作符和 map 简化流处理
     */
    fun toDTOList(entities: List<TaskEntity>): List<TaskDTO> {
        return entities.mapNotNull { toDTO(it) } ?: emptyList()
    }

}