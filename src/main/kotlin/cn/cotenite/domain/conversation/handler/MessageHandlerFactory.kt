package cn.cotenite.domain.conversation.handler

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import cn.cotenite.domain.agent.model.AgentEntity

/**
 * 消息处理器工厂
 */
@Component
class MessageHandlerFactory(
    private val applicationContext: ApplicationContext
) {

    fun getHandler(agent: AgentEntity): MessageHandler =
        applicationContext.getBean("standardMessageHandler", MessageHandler::class.java)
}
