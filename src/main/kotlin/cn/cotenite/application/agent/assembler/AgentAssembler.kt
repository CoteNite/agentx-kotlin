package cn.cotenite.application.agent.assembler

import cn.cotenite.application.agent.dto.AgentDTO
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.AgentTool
import cn.cotenite.interfaces.dto.agent.CreateAgentRequest
import cn.cotenite.interfaces.dto.agent.SearchAgentsRequest
import cn.cotenite.interfaces.dto.agent.UpdateAgentRequest
import java.time.LocalDateTime

/**
 * Agent对象组装器
 */
object AgentAssembler {

    fun toEntity(request: CreateAgentRequest, userId: String): AgentEntity {
        val now = LocalDateTime.now()
        return AgentEntity().apply {
            name = request.name
            description = request.description
            avatar = request.avatar
            systemPrompt = request.systemPrompt
            welcomeMessage = request.welcomeMessage
            agentType = request.agentType.code
            this.userId = userId
            enabled = true
            tools = request.tools.orEmpty().toMutableList()
            knowledgeBaseIds = request.knowledgeBaseIds.orEmpty().toMutableList()
            createdAt = now
            updatedAt = now
        }
    }

    fun toEntity(request: UpdateAgentRequest, userId: String): AgentEntity = AgentEntity().apply {
        id = request.agentId
        name = request.name
        description = request.description
        avatar = request.avatar
        systemPrompt = request.systemPrompt
        welcomeMessage = request.welcomeMessage
        tools = request.tools.orEmpty().toMutableList()
        knowledgeBaseIds = request.knowledgeBaseIds.orEmpty().toMutableList()
        enabled = request.enabled ?: true
        this.userId = userId
        updatedAt = LocalDateTime.now()
    }

    fun toDTO(entity: AgentEntity?): AgentDTO? = entity?.let {
        AgentDTO(
            id = it.id,
            name = it.name,
            avatar = it.avatar,
            description = it.description,
            systemPrompt = it.systemPrompt,
            welcomeMessage = it.welcomeMessage,
            tools = it.tools,
            knowledgeBaseIds = it.knowledgeBaseIds,
            publishedVersion = it.publishedVersion,
            enabled = it.enabled,
            agentType = it.agentType,
            userId = it.userId,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt
        )
    }

    fun toDTOs(agents: List<AgentEntity>?): List<AgentDTO> = agents.orEmpty().mapNotNull(::toDTO)

    fun toEntity(searchAgentsRequest: SearchAgentsRequest): AgentEntity =
        AgentEntity().apply { name = searchAgentsRequest.name }

    fun toDTO(entity: Map<String, Any?>?): AgentDTO? = entity?.let {
        AgentDTO(
            id = it["id"] as? String,
            name = it["name"] as? String,
            avatar = it["avatar"] as? String,
            description = it["description"] as? String,
            systemPrompt = it["systemPrompt"] as? String,
            welcomeMessage = it["welcomeMessage"] as? String,
            tools = (it["tools"] as? List<AgentTool>).orEmpty(),
            knowledgeBaseIds = (it["knowledgeBaseIds"] as? List<String>).orEmpty(),
            publishedVersion = it["publishedVersion"] as? String,
            enabled = it["enabled"] as? Boolean ?: true,
            agentType = it["agentType"] as? Int,
            userId = it["userId"] as? String,
            createdAt = it["createdAt"] as? LocalDateTime,
            updatedAt = it["updatedAt"] as? LocalDateTime
        )
    }
}
