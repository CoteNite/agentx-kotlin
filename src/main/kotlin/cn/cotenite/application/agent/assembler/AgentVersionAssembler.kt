package cn.cotenite.application.agent.assembler

import cn.cotenite.application.agent.dto.AgentVersionDTO
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.AgentTool
import cn.cotenite.domain.agent.model.AgentVersionEntity
import cn.cotenite.interfaces.dto.agent.PublishAgentVersionRequest
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
            tools = it.tools,
            knowledgeBaseIds = it.knowledgeBaseIds,
            changeLog = it.changeLog,
            agentType = it.agentType,
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

    fun createVersionEntity(agent: Map<String, Any?>, request: PublishAgentVersionRequest): MutableMap<String, Any?> {
        val now = LocalDateTime.now()
        return mutableMapOf(
            "agentId" to agent["id"],
            "name" to agent["name"],
            "avatar" to agent["avatar"],
            "description" to agent["description"],
            "versionNumber" to request.versionNumber,
            "systemPrompt" to agent["systemPrompt"],
            "welcomeMessage" to agent["welcomeMessage"],
            "tools" to (agent["tools"] ?: emptyList<AgentTool>()),
            "knowledgeBaseIds" to (agent["knowledgeBaseIds"] ?: emptyList<String>()),
            "changeLog" to request.changeLog,
            "agentType" to agent["agentType"],
            "publishStatus" to 1,
            "userId" to agent["userId"],
            "createdAt" to now,
            "updatedAt" to now
        )
    }
}
