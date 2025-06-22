package cn.cotenite.agentxkotlin.application.agent.service

import cn.cotenite.agentxkotlin.application.agent.assembler.AgentAssembler
import cn.cotenite.agentxkotlin.application.agent.assembler.AgentAssembler.toDTO
import cn.cotenite.agentxkotlin.application.agent.assembler.AgentAssembler.toDTOs
import cn.cotenite.agentxkotlin.application.agent.assembler.AgentAssembler.toEntity
import cn.cotenite.agentxkotlin.application.agent.assembler.AgentVersionAssembler
import cn.cotenite.agentxkotlin.application.agent.assembler.AgentVersionAssembler.createVersionEntity
import cn.cotenite.agentxkotlin.application.agent.assembler.AgentVersionAssembler.toDTO
import cn.cotenite.agentxkotlin.application.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.application.agent.dto.AgentVersionDTO
import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.domain.agent.model.AgentEntity
import cn.cotenite.agentxkotlin.domain.agent.model.AgentVersionEntity
import cn.cotenite.agentxkotlin.domain.agent.model.AgentWorkspaceEntity
import cn.cotenite.agentxkotlin.domain.agent.service.AgentDomainService
import cn.cotenite.agentxkotlin.domain.agent.service.AgentWorkspaceDomainService
import cn.cotenite.agentxkotlin.infrastructure.exception.ParamValidationException
import cn.cotenite.agentxkotlin.interfaces.dto.agent.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 15:21
 */
@Service
class AgentAppService(
    private val agentServiceDomainService: AgentDomainService,
    private val agentWorkspaceDomainService: AgentWorkspaceDomainService,
){

    @Transactional
    fun createAgent(request: CreateAgentRequest, userId: String): AgentDTO? {
        val entity = toEntity(request, userId)
        entity.userId = userId
        val agent = agentServiceDomainService.createAgent(entity)
        val agentWorkspaceEntity = AgentWorkspaceEntity()
        agentWorkspaceEntity.agentId = agent.id
        agentWorkspaceEntity.userId = userId
        agentWorkspaceDomainService.save(agentWorkspaceEntity)
        return toDTO(agent)
    }

    /**
     * 获取Agent信息
     */
    fun getAgent(agentId: String, userId: String): AgentDTO? {
        // todo 判断用户是否存在
        val agent = agentServiceDomainService.getAgent(agentId, userId)
        return toDTO(agent)
    }

    /**
     * 获取用户的Agent列表，支持状态和名称过滤
     */
    fun getUserAgents(userId: String, searchAgentsRequest: SearchAgentsRequest): List<AgentDTO> {
        val entity = toEntity(searchAgentsRequest)
        val agents= agentServiceDomainService.getUserAgents(userId, entity)
        return toDTOs(agents)
    }

    /**
     * 获取已上架的Agent列表，支持名称搜索
     */
    fun getPublishedAgentsByName(searchAgentsRequest: SearchAgentsRequest): List<AgentVersionDTO> {
        val entity = toEntity(searchAgentsRequest)
        val agentVersionEntities = agentServiceDomainService.getPublishedAgentsByName(entity)
        return AgentVersionAssembler.toDTOs(agentVersionEntities)
    }

    /**
     * 更新Agent信息（基本信息和配置合并更新）
     */
    fun updateAgent(request: UpdateAgentRequest, userId: String): AgentDTO? {
        val updateEntity = toEntity(request, userId)
        updateEntity.userId = userId
        val agentEntity = agentServiceDomainService.updateAgent(updateEntity)
        return toDTO(agentEntity)
    }

    /**
     * 切换Agent的启用/禁用状态
     */
    fun toggleAgentStatus(agentId: String): AgentDTO? {
        val agentEntity = agentServiceDomainService.toggleAgentStatus(agentId)
        return toDTO(agentEntity)
    }

    /**
     * 删除Agent
     */
    fun deleteAgent(agentId: String, userId: String) {
        agentServiceDomainService.deleteAgent(agentId, userId)
    }

    /**
     * 发布Agent版本
     */
    fun publishAgentVersion(agentId: String, request: PublishAgentVersionRequest, userId: String): AgentVersionDTO? {
        request.validate()
        val agent = agentServiceDomainService.getAgent(agentId, userId)

        // 获取最新版本，检查版本号大小
        var agentVersionEntity = agentServiceDomainService.getLatestAgentVersion(agentId)
        if (agentVersionEntity != null) {
            // 检查版本号是否大于上一个版本
            if (!request.isVersionGreaterThan(agentVersionEntity.versionNumber)) {
                throw ParamValidationException(
                    "versionNumber",
                    "新版本号(" + request.versionNumber +
                            ")必须大于当前最新版本号(" + agentVersionEntity.versionNumber + ")"
                )
            }
        }
        val versionEntity = createVersionEntity(agent, request)
        versionEntity.userId = userId
        agentVersionEntity = agentServiceDomainService.publishAgentVersion(agentId, versionEntity)
        return toDTO(agentVersionEntity)
    }

    /**
     * 获取Agent的所有版本
     */
    fun getAgentVersions(agentId: String, userId: String): List<AgentVersionDTO> {
        val agentVersions = agentServiceDomainService.getAgentVersions(agentId, userId)
        return AgentVersionAssembler.toDTOs(agentVersions)
    }

    /**
     * 获取Agent的特定版本
     */
    fun getAgentVersion(agentId: String, versionNumber: String): AgentVersionDTO? {
        val agentVersion = agentServiceDomainService.getAgentVersion(agentId, versionNumber)
        return toDTO(agentVersion)
    }

    /**
     * 获取Agent的最新版本
     */
    fun getLatestAgentVersion(agentId: String): AgentVersionDTO? {
        val latestAgentVersion = agentServiceDomainService.getLatestAgentVersion(agentId)
        return toDTO(latestAgentVersion)
    }

    /**
     * 审核Agent版本
     */
    fun reviewAgentVersion(versionId: String, request: ReviewAgentVersionRequest): AgentVersionDTO? {
        // 在应用层验证请求
        request.validate()

        var agentVersionEntity: AgentVersionEntity?
        // 根据状态执行相应操作
        if (PublishStatus.REJECTED == request.status) {
            // 拒绝发布，需使用拒绝原因
            agentVersionEntity = agentServiceDomainService.rejectVersion(versionId, request.rejectReason!!)
        } else {
            // 其他状态变更，直接更新状态
            agentVersionEntity = agentServiceDomainService.updateVersionPublishStatus(versionId, request.status!!)
        }
        return toDTO(agentVersionEntity)
    }

    /**
     * 根据发布状态获取版本列表
     *
     * @param status 发布状态
     * @return 版本列表（每个助理只返回最新版本）
     */
    fun getVersionsByStatus(status: PublishStatus?): List<AgentVersionDTO> {
        val versionsByStatus= agentServiceDomainService.getVersionsByStatus(status)
        return AgentVersionAssembler.toDTOs(versionsByStatus)
    }

}