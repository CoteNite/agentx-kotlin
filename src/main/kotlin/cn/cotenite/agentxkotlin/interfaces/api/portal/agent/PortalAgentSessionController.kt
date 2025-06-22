package cn.cotenite.agentxkotlin.interfaces.api.portal.agent

import cn.cotenite.agentxkotlin.application.agent.service.AgentSessionAppService
import cn.cotenite.agentxkotlin.application.conversation.dto.ChatRequest
import cn.cotenite.agentxkotlin.application.conversation.dto.MessageDTO
import cn.cotenite.agentxkotlin.application.conversation.dto.SessionDTO
import cn.cotenite.agentxkotlin.application.conversation.service.ConversationAppService
import cn.cotenite.agentxkotlin.infrastructure.auth.UserContext
import cn.cotenite.agentxkotlin.interfaces.api.common.Response
import org.slf4j.LoggerFactory
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

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 18:38
 */
@RestController
@RequestMapping("/agent/session")
class PortalAgentSessionController(
    // 通過構造函數注入服務
    private val agentSessionAppService: AgentSessionAppService,
    private val conversationAppService: ConversationAppService
) {
    // 使用伴生物件（companion object）來定義靜態 logger 和 ExecutorService
    // 這樣可以避免每次實例化控制器時都創建它們
    companion object {
        private val logger = LoggerFactory.getLogger(PortalAgentSessionController::class.java)
    }

    /**
     * 獲取會話中的消息列表
     */
    @GetMapping("/{sessionId}/messages")
    fun getConversationMessages(@PathVariable sessionId: String): Response<List<MessageDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(conversationAppService.getConversationMessages(sessionId, userId))
    }

    /**
     * 獲取助理會話列表
     */
    @GetMapping("/{agentId}")
    fun getAgentSessionList(@PathVariable agentId: String): Response<List<SessionDTO>> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentSessionAppService.getAgentSessionList(userId, agentId))
    }

    /**
     * 創建會話
     */
    @PostMapping("/{agentId}")
    fun createSession(@PathVariable agentId: String): Response<SessionDTO?> {
        val userId = UserContext.getCurrentUserId()
        return Response.success(agentSessionAppService.createSession(userId, agentId))
    }

    /**
     * 更新會話
     */
    @PutMapping("/{id}")
    fun updateSession(@PathVariable id: String, @RequestParam title: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        agentSessionAppService.updateSession(id, userId, title)
        return Response.success() // 保持與 Java 版本一致，返回 Void 類型
    }

    /**
     * 刪除會話
     */
    @DeleteMapping("/{id}")
    fun deleteSession(@PathVariable id: String): Response<Unit> {
        val userId = UserContext.getCurrentUserId()
        agentSessionAppService.deleteSession(id, userId)
        return Response.success() // 保持與 Java 版本一致，返回 Void 類型
    }

    /**
     * 發送消息
     * @param chatRequest 消息物件
     * @return SseEmitter 用於伺服器發送事件流
     */
    @PostMapping("/chat")
    fun chat(@RequestBody @Validated chatRequest: ChatRequest): SseEmitter {
        return conversationAppService.chat(chatRequest, UserContext.getCurrentUserId())
    }
}