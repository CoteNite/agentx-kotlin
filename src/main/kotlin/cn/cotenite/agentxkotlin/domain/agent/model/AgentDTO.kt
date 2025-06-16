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
    val modeConfig: ModelConfig?,
    val tools: MutableList<AgentTool>?,
    val knowledgeBaseIds: MutableList<String>?,
    val publishedVersion: String?,
    val enabled:Boolean,
    val agentType:Int,
    val userId:String,
    val createAt:LocalDateTime,
    val updateAt:LocalDateTime
)
