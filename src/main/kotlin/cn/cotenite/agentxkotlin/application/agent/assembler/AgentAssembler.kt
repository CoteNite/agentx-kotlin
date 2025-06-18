package cn.cotenite.agentxkotlin.application.agent.assembler

import cn.cotenite.agentxkotlin.domain.agent.model.*
import cn.cotenite.agentxkotlin.interfaces.dto.agent.CreateAgentRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.PublishAgentVersionRequest
import cn.cotenite.agentxkotlin.interfaces.dto.agent.UpdateAgentRequest
import java.time.LocalDateTime


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 02:49
 */
class AgentAssembler {

    companion object {
        /**
         * 将CreateAgentRequest转换为AgentEntity
         */
        fun toEntity(request: CreateAgentRequest, userId: String): AgentEntity {
            val now = LocalDateTime.now()
            return AgentEntity(
                name = request.name,
                userId = userId,
                description = request.description,
                avatar = request.avatar,
                systemPrompt = request.systemPrompt,
                welcomeMessage = request.welcomeMessage,
                type = request.agentType.code,
                enabled = true,
                modelConfig = request.modelConfig,
                tools = request.tools,
                knowledgeBaseIds = request.knowledgeBaseIds,
                createdAt = now,
                updatedAt = now
            )
        }


        /**
         * 将UpdateAgentRequest转换为AgentEntity
         * 注意：UpdateAgentRequest通常不包含所有字段，并且需要传入原始实体来更新。
         * 如果是完全替换，可以像这样创建新实体。但更常见的更新是基于ID获取后，再复制更新。
         * 这里的实现假设AgentEntity的构造函数接受所有可更新字段。
         */
        fun toEntity(request: UpdateAgentRequest, userId: String): AgentEntity {
            return AgentEntity(
                name = request.name,
                userId = userId,
                description = request.description,
                avatar = request.avatar,
                systemPrompt = request.systemPrompt,
                welcomeMessage = request.welcomeMessage,
                modelConfig = request.modelConfig,
                tools = request.tools,
                knowledgeBaseIds = request.knowledgeBaseIds,
            )
        }


        /**
         * 创建AgentVersionEntity
         * 这个方法保持不变，因为它已经使用了构造方法。
         */
        fun createVersionEntity(agent: AgentEntity, request: PublishAgentVersionRequest): AgentVersionEntity {
            return AgentVersionEntity.createFromAgent(agent, request.versionNumber, request.changeLog)
        }

        /**
         * 将AgentEntity转换为AgentDTO
         */
        fun toDTO(entity: AgentEntity?): AgentDTO? {
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
                    agentType = it.type,
                    userId = it.userId,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

        /**
         * 将AgentVersionEntity转换为AgentVersionDTO
         */
        fun toDTO(entity: AgentVersionEntity?): AgentVersionDTO? {
            return entity?.let {
                AgentVersionDTO(
                    id = it.id,
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
                    name = it.name,
                    avatar = it.avatar,
                    description = it.description,
                    publishStatus = it.publishStatus,
                    rejectReason = it.rejectReason,
                    reviewTime = it.reviewTime,
                    userId = it.userId,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    deletedAt = it.deletedAt
                )
            }
        }

        /**
         * 将AgentEntity列表转换为AgentDTO列表
         */
        fun toDTOList(entities: List<AgentEntity?>?): List<AgentDTO?> {
            // 使用 Kotlin 的 mapNotNull 和 Elvis 操作符简化
            return entities?.mapNotNull { toDTO(it) } ?: emptyList()
        }

        /**
         * 将AgentVersionEntity列表转换为AgentVersionDTO列表
         */
        fun toVersionDTOList(entities: List<AgentVersionEntity?>?): List<AgentVersionDTO?> {
            // 使用 Kotlin 的 mapNotNull 和 Elvis 操作符简化
            return entities?.mapNotNull { toDTO(it) } ?: emptyList()
        }
    }
}
