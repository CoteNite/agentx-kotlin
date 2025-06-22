package cn.cotenite.agentxkotlin.application.agent.assembler

import cn.cotenite.agentxkotlin.application.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.domain.agent.constant.AgentType
import cn.cotenite.agentxkotlin.domain.agent.model.*
import cn.cotenite.agentxkotlin.domain.agent.model.AgentModelConfig.Companion.createDefault
import cn.cotenite.agentxkotlin.interfaces.dto.agent.CreateAgentRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.SearchAgentsRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.UpdateAgentRequest
import java.time.LocalDateTime
import java.util.stream.Collectors


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 02:49
 */
object AgentAssembler {

    /**
     * 将CreateAgentRequest转换为AgentEntity
     */
    fun toEntity(request: CreateAgentRequest, userId: String): AgentEntity {
        val now = LocalDateTime.now()
        val entity = AgentEntity(
            name = request.name,
            description = request.description,
            avatar = request.avatar,
            systemPrompt = request.systemPrompt,
            welcomeMessage = request.welcomeMessage,
            agentType = request.agentType.code,
            userId = userId,
            enabled = true, // 初始状态为启用
            modelConfig = request.modelConfig ?: AgentModelConfig.createDefault(),
            tools = request.tools.orEmpty().toMutableList(),
            knowledgeBaseIds = request.knowledgeBaseIds.orEmpty().toMutableList(),
        )
        entity.createdAt = now
        entity.updatedAt = now
        return entity

    }

    /**
     * 将UpdateAgentRequest转换为AgentEntity
     */
    fun toEntity(request: UpdateAgentRequest, userId: String): AgentEntity {
        return AgentEntity(
            name = request.name,
            description = request.description,
            avatar = request.avatar,
            systemPrompt = request.systemPrompt,
            welcomeMessage = request.welcomeMessage,
            modelConfig = request.modelConfig,
            tools = request.tools.toMutableList(),
            knowledgeBaseIds = request.knowledgeBaseIds.toMutableList(),
            userId = userId,
            enabled = request.enabled
            // createdAt 和 updatedAt 通常在更新时会单独处理或由持久层自动更新
        )
    }

    /**
     * 将AgentEntity转换为AgentDTO
     */
    fun toDTO(entity: AgentEntity?): AgentDTO? {
        // Kotlin 允许更简洁的 null 检查，使用安全调用 ?. 和 let 表达式
        return entity?.let {
            AgentDTO(
                id = it.id,
                name = it.name,
                avatar = it.avatar,
                description = it.description,
                systemPrompt = it.systemPrompt,
                welcomeMessage = it.welcomeMessage,
                modelConfig = it.modelConfig,
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
    }

    /**
     * 将AgentEntity列表转换为AgentDTO列表
     */
    fun toDTOs(agents: List<AgentEntity>?): List<AgentDTO> {
        // 使用 mapNotNull 过滤掉 null 结果，并使用 Elvis 运算符处理空列表
        return agents?.mapNotNull { toDTO(it) } ?: emptyList()
    }

    /**
     * 将SearchAgentsRequest转换为AgentEntity
     * 注意：这里仅转换了 name 字段，如果 AgentEntity 构造器有更多非空参数，可能需要补充默认值或更复杂的逻辑
     */
    fun toEntity(searchAgentsRequest: SearchAgentsRequest): AgentEntity {
        // 创建 AgentEntity 实例，只设置 name 属性
        return AgentEntity(
            name = searchAgentsRequest.name
            // 假设 AgentEntity 的其他字段在搜索请求中可能不关心，或者有默认值
            // 如果 AgentEntity 的主构造函数有其他非空属性，这里需要提供默认值或更完善的映射
        )
    }

}

