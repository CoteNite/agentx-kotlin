package cn.cotenite.agentxkotlin.application.agent.dto

import cn.cotenite.agentxkotlin.domain.agent.constant.AgentStatus
import cn.cotenite.agentxkotlin.domain.agent.constant.AgentType
import cn.cotenite.agentxkotlin.domain.agent.model.AgentEntity
import cn.cotenite.agentxkotlin.domain.agent.model.AgentModelConfig
import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import java.time.LocalDateTime

/**
 * Agent数据传输对象，用于表示层和应用层之间传递数据
 */
data class AgentDTO(
    /**
     * Agent唯一ID
     */
    var id: String? = null,
    
    /**
     * Agent名称
     */
    var name: String? = null,
    
    /**
     * Agent头像URL
     */
    var avatar: String? = null,
    
    /**
     * Agent描述
     */
    var description: String? = null,
    
    /**
     * Agent系统提示词
     */
    var systemPrompt: String? = null,
    
    /**
     * 欢迎消息
     */
    var welcomeMessage: String? = null,
    
    /**
     * 模型配置，包含模型类型、温度等参数
     */
    var modelConfig: AgentModelConfig? = null,
    
    /**
     * Agent可使用的工具列表
     */
    var tools: MutableList<AgentTool>? = null,
    
    /**
     * 关联的知识库ID列表
     */
    var knowledgeBaseIds: MutableList<String>? = null,
    
    /**
     * 当前发布的版本ID
     */
    var publishedVersion: String? = null,
    
    /**
     * Agent状态：true-启用，false-禁用
     */
    var enabled: Boolean = true,
    
    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     */
    var agentType: Int? = null,
    
    /**
     * 创建者用户ID
     */
    var userId: String? = null,
    
    /**
     * 创建时间
     */
    var createdAt: LocalDateTime? = null,
    
    /**
     * 最后更新时间
     */
    var updatedAt: LocalDateTime? = null
) {
    init {
        if (modelConfig == null) {
            modelConfig = AgentModelConfig.createDefault()
        }
        if (tools == null) {
            tools = mutableListOf<AgentTool>()
        }
        if (knowledgeBaseIds == null) {
            knowledgeBaseIds = mutableListOf()
        }
    }
    
    /**
     * 获取状态文本描述
     */
    fun getStatusText(): String {
        return AgentStatus.fromCode(if (enabled) 1 else 0).description
    }
    
    /**
     * 获取类型文本描述
     */
    fun getAgentTypeText(): String {
        return agentType?.let { AgentType.fromCode(it) }?.description ?: ""
    }
    
    /**
     * 将当前DTO转换为Entity对象
     * @return 转换后的AgentEntity对象
     */
    fun toEntity(): AgentEntity {
        val entity = AgentEntity()
        entity.id = this.id
        entity.name = this.name
        entity.avatar = this.avatar
        entity.description = this.description
        entity.systemPrompt = this.systemPrompt
        entity.welcomeMessage = this.welcomeMessage
        entity.modelConfig = this.modelConfig
        entity.tools = this.tools
        entity.knowledgeBaseIds = this.knowledgeBaseIds
        entity.publishedVersion = this.publishedVersion
        entity.enabled = this.enabled
        entity.agentType = this.agentType ?: 0
        entity.userId = this.userId
        entity.createdAt = this.createdAt
        entity.updatedAt = this.updatedAt
        return entity
    }
}