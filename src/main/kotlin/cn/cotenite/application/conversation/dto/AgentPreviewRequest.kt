package cn.cotenite.application.conversation.dto

/** Agent预览请求DTO 用于预览尚未创建的Agent的对话效果 */
data class AgentPreviewRequest(
    /** 用户当前输入的消息 */
    var userMessage: String? = null,

    /** 系统提示词 */
    var systemPrompt: String? = null,

    /** 工具ID列表 */
    var toolIds: List<String>? = null,

    /** 工具预设参数 */
    var toolPresetParams: MutableMap<String?, MutableMap<String?, MutableMap<String?, String?>?>?>? = null,

    /** 历史消息上下文 */
    var messageHistory: List<MessageDTO>? = null,

    /** 使用的模型ID，如果为空则使用用户默认模型 */
    var modelId: String? = null,

    /** 文件列表 */
    var fileUrls: MutableList<String?> = mutableListOf()
)