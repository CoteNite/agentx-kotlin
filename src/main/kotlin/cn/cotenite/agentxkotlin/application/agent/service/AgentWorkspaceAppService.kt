package cn.cotenite.agentxkotlin.application.agent.service

import cn.cotenite.agentxkotlin.domain.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.domain.agent.service.AgentDomainService
import cn.cotenite.agentxkotlin.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.ConversationDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.SessionDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.interfaces.dto.agent.SearchAgentsRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/20 23:40
 */
@Service
class AgentWorkspaceAppService(
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val agentServiceDomainService: AgentDomainService,
    private val sessionDomainService: SessionDomainService,
    private val conversationDomainService: ConversationDomainService
) {

    /**
     * 获取工作区下的助理
     *
     * @param userId 用户id
     * @return
     */
    fun getAgents(userId: String): List<AgentDTO> {
        // 1. 获取当前用户的所有助理
        val userAgents = agentServiceDomainService.getUserAgents(userId, SearchAgentsRequest())

        // 2. 获取已添加到工作区的助理
        val workspaceAgents = agentWorkspaceDomainService.getWorkspaceAgents(userId)

        // 合并两个列表
        return userAgents + workspaceAgents
    }

    /**
     * 删除工作区中的助理
     * @param agentId 助理id
     * @param userId 用户id
     */
    @Transactional
    fun deleteAgent(agentId: String, userId: String) {
        val deleted = agentWorkspaceDomainService.deleteAgent(agentId, userId)
        if (!deleted) {
            throw BusinessException("删除助理失败")
        }

        // 查出会话列表,收集 sessionIds
        // Kotlin 集合操作更简洁，直接 map 然后 toList
        val sessionIds = sessionDomainService.getSessionsByAgentId(agentId).map { it.id }

        // 如果 sessionIds 不为空才执行删除操作，否则直接返回
        if (sessionIds.isNotEmpty()) { // 使用 isNotEmpty() 替代 isEmpty() 的反向判断
            sessionDomainService.deleteSessions(sessionIds)
            conversationDomainService.deleteConversationMessages(sessionIds)
        }
    }
}