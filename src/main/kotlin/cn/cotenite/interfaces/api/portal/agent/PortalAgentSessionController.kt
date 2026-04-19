package cn.cotenite.interfaces.api.portal.agent

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import cn.cotenite.application.agent.service.AgentSessionAppService
import cn.cotenite.application.conversation.dto.AgentPreviewRequest
import cn.cotenite.application.conversation.dto.ChatRequest
import cn.cotenite.application.conversation.dto.MessageDTO
import cn.cotenite.application.conversation.dto.SessionDTO
import cn.cotenite.application.conversation.service.ConversationAppService
import cn.cotenite.infrastructure.auth.UserContext
import cn.cotenite.interfaces.api.common.Result

/**
 * Agent会话管理控制器
 */
@RestController
@RequestMapping("/agents/sessions")
class PortalAgentSessionController(
    private val agentSessionAppService: AgentSessionAppService,
    private val conversationAppService: ConversationAppService
) {

    @GetMapping("/{sessionId}/messages")
    fun getConversationMessages(@PathVariable sessionId: String): Result<List<MessageDTO>> =
        Result.success(conversationAppService.getConversationMessages(sessionId, currentUserId()))

    @GetMapping("/{agentId}")
    fun getAgentSessionList(@PathVariable agentId: String): Result<List<SessionDTO>> =
        Result.success(agentSessionAppService.getAgentSessionList(currentUserId(), agentId))

    @PostMapping("/{agentId}")
    fun createSession(@PathVariable agentId: String): Result<SessionDTO> =
        Result.success(agentSessionAppService.createSession(currentUserId(), agentId))

    @PutMapping("/{id}")
    fun updateSession(@PathVariable id: String, @RequestParam title: String): Result<Void> {
        agentSessionAppService.updateSession(id, currentUserId(), title)
        return Result.success()
    }

    @DeleteMapping("/{id}")
    fun deleteSession(@PathVariable id: String): Result<Void> {
        agentSessionAppService.deleteSession(id, currentUserId())
        return Result.success()
    }

    @PostMapping("/chat")
    fun chat(@RequestBody @Validated chatRequest: ChatRequest): SseEmitter =
        conversationAppService.chat(chatRequest, currentUserId())

    /** Agent预览功能，用于在创建/编辑 Agent 时预览对话效果，无需保存会话 */
    @PostMapping("/preview")
    fun preview(@RequestBody previewRequest: AgentPreviewRequest): SseEmitter =
        conversationAppService.previewAgent(previewRequest, currentUserId())

    private fun currentUserId(): String = UserContext.getCurrentUserId()
}
