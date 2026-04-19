package cn.cotenite.application.agent.assembler

import cn.cotenite.application.agent.dto.AgentDTO
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.interfaces.dto.agent.request.CreateAgentRequest
import cn.cotenite.interfaces.dto.agent.request.SearchAgentsRequest
import cn.cotenite.interfaces.dto.agent.request.UpdateAgentRequest
import org.springframework.beans.BeanUtils
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
            this.userId = userId
            enabled = true
            toolIds = request.toolIds.orEmpty().toMutableList()
            knowledgeBaseIds = request.knowledgeBaseIds.orEmpty().toMutableList()
            toolPresetParams=request.toolPresetParams
            multiModal=request.multiModal
            createdAt = now
            updatedAt = now
        }
    }

    fun toEntity(request: UpdateAgentRequest, userId: String): AgentEntity {
        val entity= AgentEntity()
        BeanUtils.copyProperties(request, entity)
        entity.userId=userId
        return entity
    }

    fun toDTO(entity: AgentEntity?): AgentDTO? {
        if (entity==null){
            return null
        }
        val dto = AgentDTO()
        BeanUtils.copyProperties(entity,dto)
        return dto
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
            toolIds = (it["toolIds"] as? List<String>).orEmpty(),
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
