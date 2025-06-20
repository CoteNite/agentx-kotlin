package cn.cotenite.agentxkotlin.application.agent.assembler

import cn.cotenite.agentxkotlin.domain.agent.dto.AgentVersionDTO
import cn.cotenite.agentxkotlin.domain.agent.model.AgentEntity
import cn.cotenite.agentxkotlin.domain.agent.model.AgentVersionEntity
import cn.cotenite.agentxkotlin.interfaces.dto.agent.PublishAgentVersionRequest


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/20 23:33
 */
object AgentVersionAssembler {

    /**
     * 将AgentVersionEntity列表转换为AgentVersionDTO列表
     */
    fun toVersionDTOList(entities: MutableList<AgentVersionEntity>): MutableList<AgentVersionDTO> {

        val dtoList: MutableList<AgentVersionDTO> = ArrayList(entities.size)
        for (entity in entities) {
            dtoList.add(toDTO(entity))
        }

        return dtoList
    }

    /**
     * 将AgentVersionEntity转换为AgentVersionDTO
     */
    fun toDTO(entity: AgentVersionEntity): AgentVersionDTO {

        return AgentVersionDTO(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            avatar = entity.avatar,
            agentId = entity.agentId,
            versionNumber = entity.versionNumber,
            systemPrompt = entity.systemPrompt,
            welcomeMessage = entity.welcomeMessage,
            modelConfig = entity.modelConfig,
            tools = entity.tools,
            knowledgeBaseIds = entity.knowledgeBaseIds,
            changeLog = entity.changeLog,
            agentType = entity.agentType,
            publishedAt = entity.publishedAt,
            publishStatus = entity.publishStatus,
        )
    }


    /**
     * 创建AgentVersionEntity
     */
    fun createVersionEntity(agent: AgentEntity, request: PublishAgentVersionRequest): AgentVersionEntity {
        return AgentVersionEntity.createFromAgent(agent, request.versionNumber, request.changeLog)
    }
}