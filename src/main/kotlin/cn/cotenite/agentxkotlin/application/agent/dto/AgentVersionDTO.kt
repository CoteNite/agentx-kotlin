package cn.cotenite.agentxkotlin.application.agent.dto

import cn.cotenite.agentxkotlin.domain.agent.constant.AgentType
import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.domain.agent.model.AgentModelConfig
import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import java.time.LocalDateTime

/**
 * Agent版本数据传输对象，用于表示层和应用层之间传递Agent版本数据
 */
data class AgentVersionDTO(
    /**
     * 版本唯一ID
     */
    var id: String? = null,

    /**
     * 关联的Agent ID
     */
    var agentId: String? = null,

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
     * 版本号，如1.0.0
     */
    var versionNumber: String? = null,

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
     * 版本更新日志
     */
    var changeLog: String? = null,

    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     */
    var agentType: Int? = null,

    /**
     * 发布状态：1-审核中, 2-已发布, 3-拒绝, 4-已下架
     */
    var publishStatus: Int? = null,

    /**
     * 审核拒绝原因
     */
    var rejectReason: String? = null,

    /**
     * 审核时间
     */
    var reviewTime: LocalDateTime? = null,

    /**
     * 发布时间
     */
    var publishedAt: LocalDateTime? = null,

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
            tools = mutableListOf()
        }
        if (knowledgeBaseIds == null) {
            knowledgeBaseIds = mutableListOf()
        }
    }
    
    /**
     * 获取类型文本描述
     */
    fun getAgentTypeText(): String {
        return agentType?.let { AgentType.fromCode(it) }?.description?:""
    }
    
    /**
     * 获取发布状态的描述文本
     */
    fun getPublishStatusText(): String {
        return publishStatus?.let { PublishStatus.fromCode(it) }?.description ?: ""
    }
    
    /**
     * 是否已发布状态
     */
    fun isPublished(): Boolean {
        return PublishStatus.PUBLISHED.code == publishStatus
    }
    
    /**
     * 是否被拒绝状态
     */
    fun isRejected(): Boolean {
        return PublishStatus.REJECTED.code == publishStatus
    }
    
    /**
     * 是否审核中状态
     */
    fun isReviewing(): Boolean {
        return PublishStatus.REVIEWING.code == publishStatus
    }
    
    /**
     * 是否已下架状态
     */
    fun isRemoved(): Boolean {
        return PublishStatus.REMOVED.code == publishStatus
    }
}