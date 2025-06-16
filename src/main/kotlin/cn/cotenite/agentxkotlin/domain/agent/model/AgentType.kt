package cn.cotenite.agentxkotlin.domain.agent.model

import cn.cotenite.agentxkotlin.domain.common.exception.BusinessException

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 11:35
 */
enum class AgentType(
    val code:Int,
    val description:String
){

    /**
     * 聊天助手
     */
    CHAT_ASSISTANT(1, "聊天助手"),

    /**
     * 功能性Agent
     */
    FUNCTIONAL_AGENT(2, "功能性Agent");


    companion object{

        fun fromCode(code: Int): AgentType {
            return AgentType.entries.firstOrNull { it.code == code } ?: CHAT_ASSISTANT
        }
    }



}
