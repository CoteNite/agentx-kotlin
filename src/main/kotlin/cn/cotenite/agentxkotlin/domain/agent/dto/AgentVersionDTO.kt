package cn.cotenite.agentxkotlin.domain.agent.dto

import cn.cotenite.agentxkotlin.domain.agent.constant.AgentType
import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import cn.cotenite.agentxkotlin.domain.agent.model.ModelConfig
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import java.time.LocalDateTime

data class AgentVersionDTO(
    /**
     * 版本唯一ID
     */
    var id: String?, // var 表示可变， 表示可空，默认 null

    /**
     * 关联的Agent ID
     */
    var agentId: String,

    /**
     * Agent名称
     */
    var name: String,

    /**
     * Agent头像URL
     */
    var avatar: String?,

    /**
     * Agent描述
     */
    var description: String?,

    /**
     * 版本号，如1.0.0
     */
    var versionNumber: String,

    /**
     * Agent系统提示词
     */
    var systemPrompt: String?,

    /**
     * 欢迎消息
     */
    var welcomeMessage: String?,

    /**
     * 模型配置，包含模型类型、温度等参数
     * 默认值设置为 ModelConfig.createDefault()，与 Java 无参构造函数逻辑一致
     */
    var modelConfig: ModelConfig? = ModelConfig(),

    /**
     * Agent可使用的工具列表
     * 默认值设置为可变空列表，与 Java 无参构造函数逻辑一致
     */
    var tools: MutableList<AgentTool>? = mutableListOf(), // 默认值，不可为空

    /**
     * 关联的知识库ID列表
     * 默认值设置为可变空列表，与 Java 无参构造函数逻辑一致
     */
    var knowledgeBaseIds: MutableList<String>? = mutableListOf(), // 默认值，不可为空

    /**
     * 版本更新日志
     */
    var changeLog: String?,

    /**
     * Agent类型：1-聊天助手, 2-功能性Agent
     * 使用 Int 表示可空，但如果您有 AgentType 枚举，可以考虑直接使用 AgentType 类型
     */
    var agentType: Int?,

    /**
     * 发布状态：1-审核中, 2-已发布, 3-拒绝, 4-已下架
     * 使用 Int 表示可空，但如果您有 PublishStatus 枚举，可以考虑直接使用 PublishStatus 类型
     */
    var publishStatus: Int?,

    /**
     * 审核拒绝原因
     */
    var rejectReason: String?=null,

    /**
     * 审核时间
     */
    var reviewTime: LocalDateTime?=null,

    /**
     * 发布时间
     */
    var publishedAt: LocalDateTime?,

    /**
     * 创建者用户ID
     */
    var userId: String?=null,

    /**
     * 创建时间
     */
    var createdAt: LocalDateTime?=null,

    /**
     * 最后更新时间
     */
    var updatedAt: LocalDateTime?=null,

    /**
     * 删除时间（软删除）
     */
    var deletedAt: LocalDateTime?=null
) {

    val agentTypeText: String
        get() = agentType.let {
            if (it == null){
                throw BusinessException("INVALID_TYPE_CODE", "无效的Agent类型码")
            }
            AgentType.fromCode(it).description
        }

    /**
     * 获取发布状态的描述文本
     * 计算属性，根据 publishStatus 返回 PublishStatus 的描述
     */
    val publishStatusText: String
        get() = publishStatus.let {
            if (it == null) {
                throw BusinessException("INVALID_STATUS_CODE", "无效的发布状态码")
            }
            PublishStatus.fromCode(it).description
        }

    /**
     * 是否已发布状态
     */
    fun isPublished(): Boolean {
        return publishStatus == PublishStatus.PUBLISHED.code
    }

    /**
     * 是否被拒绝状态
     */
    fun isRejected(): Boolean {
        return publishStatus == PublishStatus.REJECTED.code
    }

    /**
     * 是否审核中状态
     */
    fun isReviewing(): Boolean {
        return publishStatus == PublishStatus.REVIEWING.code
    }

    /**
     * 是否已下架状态
     */
    fun isRemoved(): Boolean {
        return publishStatus == PublishStatus.REMOVED.code
    }
}
