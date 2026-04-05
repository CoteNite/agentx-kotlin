package cn.cotenite.application.agent.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import cn.cotenite.application.agent.assembler.AgentAssembler
import cn.cotenite.application.agent.assembler.AgentWorkspaceAssembler
import cn.cotenite.application.agent.dto.AgentDTO
import cn.cotenite.domain.agent.constant.PublishStatus
import cn.cotenite.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.domain.agent.model.LLMModelConfig
import cn.cotenite.domain.agent.service.AgentDomainService
import cn.cotenite.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.domain.conversation.service.ConversationDomainService
import cn.cotenite.domain.conversation.service.SessionDomainService
import cn.cotenite.domain.llm.service.LlmDomainService
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.interfaces.dto.agent.request.UpdateModelConfigRequest

/**
 * Agent工作区应用服务
 */
@Service
class AgentWorkspaceAppService(
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
    private val agentDomainService: AgentDomainService,
    private val sessionDomainService: SessionDomainService,
    private val conversationDomainService: ConversationDomainService,
    private val llmDomainService: LlmDomainService
) {

    fun getAgents(userId: String): List<AgentDTO> =
        agentWorkspaceDomainService.getWorkspaceAgents(userId).let(AgentAssembler::toDTOs)

    @Transactional
    fun deleteAgent(agentId: String, userId: String) {
        val agent = agentDomainService.getAgentById(agentId)
        if (agent.userId == userId) throw BusinessException("该助理属于自己，不允许删除")

        if (!agentWorkspaceDomainService.deleteAgent(agentId, userId)) {
            throw BusinessException("删除助理失败")
        }

        val sessionIds = sessionDomainService.getSessionsByAgentId(agentId).mapNotNull { it.id }
        if (sessionIds.isEmpty()) {
            return
        }

        sessionDomainService.deleteSessions(sessionIds)
        conversationDomainService.deleteConversationMessages(sessionIds)
    }

    fun getConfiguredModelId(agentId: String, userId: String): LLMModelConfig =
        agentWorkspaceDomainService.getWorkspace(agentId, userId).llmModelConfig

    fun updateModelConfig(agentId: String, userId: String, request: UpdateModelConfigRequest) {
        val llmModelConfig = AgentWorkspaceAssembler.toLLMModelConfig(request)
        val modelId = llmModelConfig.modelId ?: throw BusinessException("模型不存在")

        val model = llmDomainService.getModelById(modelId)
        model.isActive()
        val provider = llmDomainService.getProvider(model.providerId ?: throw BusinessException("模型提供商不存在"))
        provider.isActive()

        agentWorkspaceDomainService.update(AgentWorkspaceEntity(agentId, userId, llmModelConfig))
    }

    fun addAgent(agentId: String, userId: String) {
        val agent = agentDomainService.getAgentById(agentId)
        if (agent.userId == userId) {
            throw BusinessException("不可添加自己的助理")
        }
        if (agentWorkspaceDomainService.exist(agentId, userId)) {
            throw BusinessException("不可重复添加助理")
        }
        if (!agent.enabled) {
            throw BusinessException("助理已禁用")
        }

        val publishedVersion = agent.publishedVersion
        val agentVersionEntity = publishedVersion?.let(agentDomainService::getAgentVersionById)
            ?: throw BusinessException("助理未发布")
        if (agentVersionEntity.getPublishStatusEnum() != PublishStatus.PUBLISHED) {
            throw BusinessException("助理未发布")
        }

        agentWorkspaceDomainService.save(AgentWorkspaceEntity(agentId, userId, LLMModelConfig()))
    }
}
