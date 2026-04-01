package cn.cotenite.domain.conversation.constant

/**
 * @author  yhk
 * Description  
 * Date  2026/3/29 17:17
 */
enum class MessageType {

    /**
     * 普通文本消息
     */
    TEXT,

    /**
     * 工具调用消息
     */
    TOOL_CALL,

    /**
     * 任务执行消息
     */
    TASK_EXEC,

    /**
     * 任务状态进行中
     */
    TASK_STATUS_TO_LOADING,

    /**
     * 任务状态完成
     */
    TASK_STATUS_TO_FINISH,

    /**
     * 任务拆分结束消息
     */
    TASK_SPLIT_FINISH
    
}