package cn.cotenite.application.conversation.service.message.chat

import cn.cotenite.application.conversation.service.message.AbstractMessageHandler
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import org.springframework.stereotype.Component

/**
 * @author  yhk
 * Description  
 * Date  2026/4/1 18:14
 */
@Component(value = "chatMessageHandler")
class ChatMessageHandler(
    override val llmServiceFactory: LLMServiceFactory,
    override val messageDomainService: MessageDomainService,
): AbstractMessageHandler(llmServiceFactory, messageDomainService){

}