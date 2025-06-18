package cn.cotenite.agentxkotlin.domain.agent.model

import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 02:53
 */
data class AgentDTO(
    val id:String,
    val name:String,
    val avatar: String?,
    val description: String?,
    val systemPrompt: String?,
    val welcomeMessage: String?,
    val modelConfig: ModelConfig?,
    val tools: MutableList<AgentTool>?,
    val knowledgeBaseIds: MutableList<String>?,
    val publishedVersion: String?,
    val enabled:Boolean,
    val agentType:Int,
    val userId:String,
    val createdAt:LocalDateTime,
    val updatedAt:LocalDateTime
){

    fun toEntity(): AgentEntity {
        return AgentEntity(
            id = this.id,
            name = this.name,
            avatar = this.avatar,
            description = this.description,
            systemPrompt = this.systemPrompt,
            welcomeMessage = this.welcomeMessage,
            modelConfig = this.modelConfig,
            tools = this.tools,
            knowledgeBaseIds = this.knowledgeBaseIds,
            publishedVersion = this.publishedVersion,
            enabled = this.enabled,
            agentType = this.agentType,
            userId = this.userId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

}
