package cn.cotenite.application.scheduledtask.assembler

import cn.cotenite.application.scheduledtask.dto.ScheduledTaskDTO
import cn.cotenite.domain.scheduledtask.constant.ScheduleTaskStatus
import cn.cotenite.domain.scheduledtask.model.ScheduledTaskEntity
import cn.cotenite.interfaces.dto.scheduledtask.request.CreateScheduledTaskRequest
import cn.cotenite.interfaces.dto.scheduledtask.request.UpdateScheduledTaskRequest

/** 定时任务组装器 负责DTO和实体之间的转换 */
object ScheduledTaskAssembler {

    /** 将创建请求转换为实体 */
    fun toEntity(request: CreateScheduledTaskRequest, userId: String): ScheduledTaskEntity =
        ScheduledTaskEntity().apply {
            this.userId = userId
            agentId = request.agentId
            sessionId = request.sessionId
            content = request.content
            repeatType = request.repeatType
            repeatConfig = request.repeatConfig
            status = ScheduleTaskStatus.ACTIVE
        }

    /** 将更新请求转换为实体 */
    fun toEntity(request: UpdateScheduledTaskRequest, userId: String): ScheduledTaskEntity =
        ScheduledTaskEntity().apply {
            id = request.id
            this.userId = userId
            content = request.content
            repeatType = request.repeatType
            repeatConfig = request.repeatConfig
            status = request.status
        }

    /** 将实体转换为DTO */
    fun toDTO(entity: ScheduledTaskEntity?): ScheduledTaskDTO? {
        entity ?: return null
        return ScheduledTaskDTO().apply {
            id = entity.id
            userId = entity.userId
            agentId = entity.agentId
            sessionId = entity.sessionId
            content = entity.content
            repeatType = entity.repeatType
            repeatConfig = entity.repeatConfig
            status = entity.status
            lastExecuteTime = entity.lastExecuteTime
            nextExecuteTime = entity.nextExecuteTime
            createdAt = entity.createdAt
            updatedAt = entity.updatedAt
        }
    }

    /** 将实体列表转换为DTO列表 */
    fun toDTOs(entities: List<ScheduledTaskEntity>?): List<ScheduledTaskDTO>? =
        entities?.map { toDTO(it)!! }
}
