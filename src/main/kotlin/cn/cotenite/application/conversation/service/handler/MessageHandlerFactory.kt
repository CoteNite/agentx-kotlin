package cn.cotenite.application.conversation.service.handler

import cn.cotenite.application.conversation.service.message.AbstractMessageHandler
import cn.cotenite.domain.agent.model.AgentEntity
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

enum class MessageHandlerType {
    STANDARD,
    AGENT
}

/**
 * 消息处理器工厂
 */
@Component
class MessageHandlerFactory(
    private val applicationContext: ApplicationContext
) {
    /**
     * 根据智能体获取合适的消息处理器
     *
     * @param agent 智能体实体
     * @return 消息处理器
     */
    fun getHandler(agent: AgentEntity): AbstractMessageHandler {
        return when (agent.agentType) {
            1 -> getHandlerByType(MessageHandlerType.STANDARD)
            2 -> getHandlerByType(MessageHandlerType.AGENT)
            else -> getHandlerByType(MessageHandlerType.STANDARD)
        }
    }

    private fun getHandlerByType(type: MessageHandlerType): AbstractMessageHandler {
        return when(type){
            MessageHandlerType.AGENT -> applicationContext.getBean<AbstractMessageHandler>("agentMessageHandler")
            else -> applicationContext.getBean<AbstractMessageHandler>("chatMessageHandler")
        }
    }

}


