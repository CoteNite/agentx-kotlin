package cn.cotenite.application.conversation.service.message.chat

import cn.cotenite.application.conversation.service.message.AbstractMessageHandler
import cn.cotenite.domain.conversation.service.MessageDomainService
import cn.cotenite.domain.llm.service.HighAvailabilityDomainService
import cn.cotenite.infrastructure.llm.LLMServiceFactory
import cn.cotenite.infrastructure.storage.OssUploadService
import org.springframework.stereotype.Component

@Component(value = "chatMessageHandler")
class ChatMessageHandler(
    override val llmServiceFactory: LLMServiceFactory,
    override val messageDomainService: MessageDomainService,
    override val ossUploadService: OssUploadService,
    override val highAvailabilityDomainService: HighAvailabilityDomainService
) : AbstractMessageHandler(
    llmServiceFactory,
    messageDomainService,
    ossUploadService,
    highAvailabilityDomainService
)
