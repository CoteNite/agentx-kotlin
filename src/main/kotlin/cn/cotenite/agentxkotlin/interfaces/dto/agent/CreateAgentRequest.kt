package cn.cotenite.agentxkotlin.interfaces.dto.agent

import cn.cotenite.agentxkotlin.domain.agent.model.AgentTool
import cn.cotenite.agentxkotlin.domain.agent.model.AgentType
import cn.cotenite.agentxkotlin.domain.agent.model.ModelConfig
import cn.cotenite.agentxkotlin.domain.common.util.ValidationUtils

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 13:56
 */
data class CreateAgentRequest(
    /**
     * Agent名称
     */
    var name: String,

    /**
     * Agent描述
     */
    var description: String,

    /**
     * Agent头像URL
     */
    var avatar: String,

    /**
     * Agent类型：默认是聊天助手
     * 如果 agentType 字段数据库定义为可空，且允许前端不传，这里用 AgentType = AgentType.CHAT_ASSISTANT
     * 但通常请求对象会明确一个默认值，所以这里直接设置为非空 AgentType，并给出默认值。
     */
    var agentType: AgentType = AgentType.CHAT_ASSISTANT,

    /**
     * Agent系统提示词
     */
    var systemPrompt: String,

    /**
     * 欢迎消息
     */
    var welcomeMessage: String,

    /**
     * 模型配置，包含模型类型、温度等参数
     * 如果 ModelConfig 必须提供，则 ModelConfig，否则 ModelConfig
     * 考虑到 ModelConfig 通常会有默认配置，这里设为非空，但如果允许不传，应改为 ModelConfig
     */
    var modelConfig: ModelConfig,

    /**
     * Agent可使用的工具列表
     */
    var tools: MutableList<AgentTool>,

    /**
     * 关联的知识库ID列表
     */
    var knowledgeBaseIds: MutableList<String>
){

    var agentTypeCode: Int?
        get() = agentType.code
        set(value) { agentType = value?.let { AgentType.fromCode(it) } ?: AgentType.CHAT_ASSISTANT }

    fun validate() {
        ValidationUtils.notEmpty(name, "name")
        ValidationUtils.length(name,1,50,"name")
    }
}
