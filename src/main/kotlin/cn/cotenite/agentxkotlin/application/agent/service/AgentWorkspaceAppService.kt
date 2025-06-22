package cn.cotenite.agentxkotlin.application.agent.service

import cn.cotenite.agentxkotlin.application.agent.assembler.AgentAssembler
import cn.cotenite.agentxkotlin.application.agent.assembler.AgentWorkspaceAssembler
import cn.cotenite.agentxkotlin.application.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.domain.agent.model.AgentEntity
import cn.cotenite.agentxkotlin.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.agentxkotlin.domain.agent.service.AgentDomainService
import cn.cotenite.agentxkotlin.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.agentxkotlin.domain.conversation.model.SessionEntity
import cn.cotenite.agentxkotlin.domain.conversation.service.ConversationDomainService
import cn.cotenite.agentxkotlin.domain.conversation.service.SessionDomainService
import cn.cotenite.agentxkotlin.domain.llm.model.config.LLMModelConfig
import cn.cotenite.agentxkotlin.domain.llm.service.LlmDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.interfaces.dto.agent.request.UpdateModelConfigRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.stream.Collectors


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
     * 获取工作区下的助理
     *
     * @param  userId 用户id
     * @return AgentDTO
     */
    fun getAgents(userId: String): List<AgentDTO> {
        val workspaceAgents = agentWorkspaceDomainService.getWorkspaceAgents(userId)
        return AgentAssembler.toDTOs(workspaceAgents)
    }

    /**
     * 删除工作区中的助理
     * @param agentId 助理id
     * @param userId 用户id
     */
    @Transactional
    fun deleteAgent(agentId: String, userId: String) {
        // agent如果是自己的则不允许删除

        val agent = agentServiceDomainService.getAgentById(agentId)
        if (agent.userId == userId) {
            throw BusinessException("该助理属于自己，不允许删除")
        }

        val deleteAgent = agentWorkspaceDomainService.deleteAgent(agentId, userId)
        if (!deleteAgent) {
            throw BusinessException("删除助理失败")
        }
        val sessionIds =
            sessionDomainService.getSessionsByAgentId(agentId).stream().map<String?>(SessionEntity::id).collect(
                Collectors.toList()
            )
        if (sessionIds.isEmpty()) {
            return
        }
        sessionDomainService.deleteSessions(sessionIds)
        conversationDomainService.deleteConversationMessages(sessionIds)
    }


    fun getConfiguredModelId(agentId: String, userId: String): LLMModelConfig {
        return agentWorkspaceDomainService.getWorkspace(agentId, userId).llmModelConfig
    }

    /**
     * 保存agent的模型配置
     * @param agentId agent ID
     * @param userId 用户ID
     * @param request 模型配置
     */
    fun updateModelConfig(agentId: String, userId: String, request: UpdateModelConfigRequest) {
        val llmModelConfig: LLMModelConfig = AgentWorkspaceAssembler.toLLMModelConfig(request)
        val modelId = llmModelConfig.modelId?:throw BusinessException("模型ID不能为空")

        // 激活校验
        val model = llmDomainService.getModelById(modelId)
        model.isActive()
        val provider = llmDomainService.getProvider(
            providerId = model.providerId,
            userId = userId
        )
        provider.isActive()

        agentWorkspaceDomainService.save(AgentWorkspaceEntity(agentId=agentId, userId = userId, llmModelConfig = llmModelConfig))
    }

}