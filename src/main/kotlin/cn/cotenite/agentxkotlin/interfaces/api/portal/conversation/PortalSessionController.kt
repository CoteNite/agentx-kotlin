package cn.cotenite.agentxkotlin.interfaces.api.portal.conversation

import cn.cotenite.agentxkotlin.application.conversation.service.ConversationAppService
import cn.cotenite.agentxkotlin.application.conversation.service.MessageAppService
import cn.cotenite.agentxkotlin.application.conversation.service.SessionAppService
import cn.cotenite.agentxkotlin.domain.conversation.model.MessageDTO
import cn.cotenite.agentxkotlin.domain.conversation.model.SessionDTO
import cn.cotenite.agentxkotlin.infrastructure.auth.UserContext
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import cn.cotenite.agentxkotlin.interfaces.dto.conversation.CreateAndChatRequest
import cn.cotenite.agentxkotlin.interfaces.dto.conversation.CreateSessionRequest
import cn.cotenite.agentxkotlin.interfaces.dto.conversation.SendMessageRequest
import cn.cotenite.agentxkotlin.interfaces.dto.conversation.UpdateSessionRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/19 00:43
 */
@RestController
@RequestMapping("/conversation")
class PortalSessionController(
    private val conversationAppService: ConversationAppService,
    private val sessionAppService: SessionAppService,
    private val messageAppService: MessageAppService
) {


    /**
     * 创建新会话
     */
    @PostMapping("/session")
    fun createSession(@RequestBody request: CreateSessionRequest): Response<SessionDTO> {
        val userId = UserContext.getCurrentUserId()
        val session = sessionAppService.createSession(
            request.title,
            userId,
            request.description
        )
        return Response.success(session)
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/session")
    fun getSessions(@RequestParam(required = false) archived: Boolean?): Response<List<SessionDTO>> {
        val userId = UserContext.getCurrentUserId()
        val sessions=if (archived != null) {
            if (archived) {
                sessionAppService.getUserArchivedSessions(userId)
            } else {
                sessionAppService.getUserActiveSessions(userId)
            }
        } else {
            sessionAppService.getUserSessions(userId)
        }
        return Response.success(sessions)
    }

    /**
     * 获取单个会话
     */
    @GetMapping("/session/{sessionId}")
    fun getSession(@PathVariable sessionId: String): Response<SessionDTO> {
        return Response.success(sessionAppService.getSession(sessionId))
    }

    /**
     * 更新会话
     */
    @PutMapping("/session/{sessionId}")
    fun updateSession(
        @PathVariable sessionId: String,
        @RequestBody request: UpdateSessionRequest
    ): Response<SessionDTO> {
        return Response.success(
            sessionAppService.updateSession(
                sessionId,
                request.title,
                request.description
            )
        )
    }

    /**
     * 归档会话
     */
    @PutMapping("/session/{sessionId}/archive")
    fun archiveSession(@PathVariable sessionId: String): Response<SessionDTO> {
        return Response.success(sessionAppService.archiveSession(sessionId))
    }

    /**
     * 恢复归档会话
     */
    @PutMapping("/session/{sessionId}/unarchive")
    fun unarchiveSession(@PathVariable sessionId: String): Response<SessionDTO> {
        return Response.success(sessionAppService.unArchiveSession(sessionId))
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    fun deleteSession(@PathVariable sessionId: String): Response<Unit> {
        sessionAppService.deleteSession(sessionId)
        return Response.success()
    }

    /**
     * 获取会话消息
     */
    @GetMapping("/session/{sessionId}/messages")
    fun getSessionMessages(@PathVariable sessionId: String): Response<List<MessageDTO>> {
        return Response.success(messageAppService.getSessionMessages(sessionId))
    }

    /**
     * 发送消息并获取流式回复
     */
    @PostMapping("/chat/{sessionId}")
    fun chat(@PathVariable sessionId: String?, @RequestBody request: SendMessageRequest): SseEmitter {
        return conversationAppService.chat("ae37ce1eba445259cc55c6740105c688", request.content)
    }

    /**
     * 创建会话并发送第一条消息
     */
    @PostMapping("/session/create-and-chat")
    fun createAndChat(@RequestBody request: CreateAndChatRequest): SseEmitter {
        return conversationAppService.createSessionAndChat(
            request.title,
            request.userId,
            request.content
        )
    }

    /**
     * 发送消息并获取同步回复(非流式)
     */
    @PostMapping("/chat/{sessionId}/sync")
    fun chatSync(
        @PathVariable sessionId: String,
        @RequestBody request: SendMessageRequest
    ): ResponseEntity<MessageDTO> {
        return ResponseEntity.ok(conversationAppService.chatSync(sessionId, request.content))
    }

    /**
     * 清除会话上下文
     */
    @PostMapping("/session/{sessionId}/clear-context")
    fun clearContext(@PathVariable sessionId: String): ResponseEntity<Unit> {
        conversationAppService.clearContext(sessionId)
        return ResponseEntity.ok().build()
    }

}
