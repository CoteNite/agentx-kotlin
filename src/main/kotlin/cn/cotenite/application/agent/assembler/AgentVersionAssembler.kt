package cn.cotenite.application.agent.assembler

import cn.cotenite.application.agent.dto.AgentVersionDTO
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.AgentTool
import cn.cotenite.domain.agent.model.AgentVersionEntity
import cn.cotenite.interfaces.dto.agent.request.PublishAgentVersionRequest
import java.time.LocalDateTime

/**
 * Agent版本对象组装器
 */
object AgentVersionAssembler {

    fun toDTO(entity: AgentVersionEntity?): AgentVersionDTO? = entity?.let {
        AgentVersionDTO(
            id = it.id,
            agentId = it.agentId,
            name = it.name,
            avatar = it.avatar,
            description = it.description,
            versionNumber = it.versionNumber,
            systemPrompt = it.systemPrompt,
            welcomeMessage = it.welcomeMessage,
            toolIds = it.toolIds,
            knowledgeBaseIds = it.knowledgeBaseIds,
            changeLog = it.changeLog,
            publishStatus = it.publishStatus,
            rejectReason = it.rejectReason,
            reviewTime = it.reviewTime,
            publishedAt = it.publishedAt,
            userId = it.userId,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt
        )
    }

    fun toDTOs(agents: List<AgentVersionEntity>?): List<AgentVersionDTO> = agents.orEmpty().mapNotNull(::toDTO)

    fun createVersionEntity(agent: AgentEntity, request: PublishAgentVersionRequest): AgentVersionEntity =
        AgentVersionEntity.createFromAgent(agent, request.versionNumber, request.changeLog)
}
