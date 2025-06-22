package cn.cotenite.agentxkotlin.domain.agent.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 02:57
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AgentModelConfig(
    /**
     * 模型名称，如：gpt-4-0125-preview, claude-3-opus-20240229等
     */
    var modelName: String? = null,

    /**
     * 温度参数，范围0-2，值越大创造性越强，越小则越保守
     */
    var temperature: Double? = null,

    /**
     * Top P参数，范围0-1，控制输出的多样性
     */
    var topP: Double? = null,

    /**
     * 最大令牌数，控制生成的内容长度
     */
    var maxTokens: Int? = null,

    /**
     * 是否启用记忆功能
     */
    var loadMemory: Boolean? = null,

    /**
     * 系统消息（仅对特定模型有效）
     */
    var systemMessage: String? = null
) {
    // data class 会自动生成无参构造函数（如果所有主构造函数参数都有默认值），
    // 同时也自动生成全参构造函数（主构造函数）以及 getter/setter。

    /**
     * 创建默认配置
     */
    companion object { // 伴生对象，用于定义静态方法
        @JvmStatic // 使得这个方法可以在 Java 代码中像静态方法一样直接调用
        fun createDefault(): AgentModelConfig {
            return AgentModelConfig(
                modelName = "gpt-3.5-turbo",
                temperature = 0.7,
                topP = 1.0,
                maxTokens = 2000,
                loadMemory = true,
                systemMessage = null // 默认不设置系统消息
            )
        }
    }
}
