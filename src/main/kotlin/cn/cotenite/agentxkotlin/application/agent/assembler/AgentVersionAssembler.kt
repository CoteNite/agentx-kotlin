package cn.cotenite.agentxkotlin.application.agent.assembler

import cn.cotenite.agentxkotlin.application.agent.dto.AgentVersionDTO
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
     * 将 AgentVersionEntity 列表转换为 AgentVersionDTO 列表
     */
    fun toVersionDTOList(entities: List<AgentVersionEntity>?): List<AgentVersionDTO> {
        // Kotlin 惯用法：如果 entities 为 null 或为空，则返回空列表
        return entities?.mapNotNull { toDTO(it) } ?: emptyList()
    }

    /**
     * 将 AgentVersionEntity 转换为 AgentVersionDTO
     */
    fun toDTO(entity: AgentVersionEntity?): AgentVersionDTO? {
        return entity?.let {
            AgentVersionDTO(
                id = it.id,
                name = it.name,
                description = it.description,
                avatar = it.avatar,
                agentId = it.agentId,
                versionNumber = it.versionNumber,
                systemPrompt = it.systemPrompt,
                welcomeMessage = it.welcomeMessage,
                modelConfig = it.modelConfig,
                tools = it.tools,
                knowledgeBaseIds = it.knowledgeBaseIds,
                changeLog = it.changeLog,
                agentType = it.agentType,
                publishedAt = it.publishedAt,
                publishStatus = it.publishStatus
            )
        }
    }

    /**
     * 创建 AgentVersionEntity
     */
    fun createVersionEntity(agent: AgentEntity, request: PublishAgentVersionRequest): AgentVersionEntity {
        return AgentVersionEntity.createFromAgent(agent, request.versionNumber, request.changeLog)
    }

    // 实际上 toVersionDTOList 和 toDTOs 功能重复，推荐保留一个。
    // 这里保留 toDTOs 并简化，与 toVersionDTOList 功能一致。
    /**
     * 将 AgentVersionEntity 列表转换为 AgentVersionDTO 列表
     */
    fun toDTOs(agents: List<AgentVersionEntity>?): List<AgentVersionDTO> {
        // 更简洁的写法，与 toVersionDTOList 保持一致的逻辑
        return agents?.mapNotNull { toDTO(it) } ?: emptyList()
    }
}