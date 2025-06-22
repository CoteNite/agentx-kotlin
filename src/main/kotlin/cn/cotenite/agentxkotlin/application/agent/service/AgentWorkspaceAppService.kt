package cn.cotenite.agentxkotlin.application.agent.service

import cn.cotenite.agentxkotlin.application.agent.assembler.AgentAssembler
import cn.cotenite.agentxkotlin.application.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.agentxkotlin.domain.agent.service.AgentDomainService
import cn.cotenite.agentxkotlin.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.ConversationDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.SessionDomainService
import cn.cotenite.agentxkotlin.domain.llm.service.LlmDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

/**
 * @Author  RichardYoung
 * @Description  
 * @Date  2025/6/22 19:42
*/
@Service
class AgentWorkspaceAppService(
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val agentServiceDomainService: AgentDomainService,
    private val sessionDomainService: SessionDomainService,
    private val conversationDomainService: ConversationDomainService,
    private val llmDomainService: LlmDomainService
){

    /**
     * 獲取工作區下的助理
     *
     * @param userId 用戶ID
     * @return AgentDTO
     */
    fun getAgents(userId: String): List<AgentDTO> {
        val workspaceAgents = agentWorkspaceDomainService.getWorkspaceAgents(userId)
        return AgentAssembler.toDTOs(workspaceAgents)
    }

    /**
     * 刪除工作區中的助理
     * @param agentId 助理ID
     * @param userId 用戶ID
     */
    @Transactional
    fun deleteAgent(agentId: String, userId: String) {

        // agent 如果是自己的則不允許刪除
        val agent = agentServiceDomainService.getAgentById(agentId)
        // Kotlin 直接使用 == 比較內容， != 比較內容不相等，對於引用類型行為與 Java equals() 類似
        if (agent.userId == userId) {
            throw BusinessException("該助理屬於自己，不允許刪除")
        }

        val deletedAgent = agentWorkspaceDomainService.deleteAgent(agentId, userId)
        if (!deletedAgent) { // 直接判斷布林值
            throw BusinessException("刪除助理失敗")
        }

        val sessionIds = sessionDomainService.getSessionsByAgentId(agentId).mapNotNull { it.id }

        if (sessionIds.isEmpty()) {
            return
        }

        sessionDomainService.deleteSessions(sessionIds)
        conversationDomainService.deleteConversationMessages(sessionIds)
    }

    /**
     * 保存模型
     * @param agentId Agent ID
     * @param userId 用戶ID
     * @param modelId 模型ID
     */
    fun saveModel(agentId: String, userId: String, modelId: String) {

        // 模型是否是自己的 or 官方的
        val model = llmDomainService.getModelById(modelId)
        // 直接訪問布林屬性 isOfficial，並使用 || 運算符
        if (!model.isOfficial && model.userId != userId) {
            throw BusinessException("模型不存在或無權使用") // 更好的錯誤訊息
        }

        // 使用 Elvis 運算符簡化 findWorkspace 和新建物件的邏輯
        val workspace = agentWorkspaceDomainService.findWorkspace(agentId, userId) ?: AgentWorkspaceEntity(
            agentId = agentId,
            userId = userId
            // 其他屬性如果有默認值則可以省略，否則在此處賦值
        )

        workspace.modelId = modelId // 直接賦值
        agentWorkspaceDomainService.save(workspace)
    }

    /**
     * 獲取配置的模型ID
     */
    fun getConfiguredModelId(agentId: String, userId: String): String? { // 返回類型為 String? 以適應 getModelId() 可能返回 null 的情況
        return agentWorkspaceDomainService.getWorkspace(agentId, userId).modelId
    }

}