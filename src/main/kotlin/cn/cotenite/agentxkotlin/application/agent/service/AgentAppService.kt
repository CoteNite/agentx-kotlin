package cn.cotenite.agentxkotlin.application.agent.service

import cn.cotenite.agentxkotlin.application.agent.assembler.AgentAssembler
import cn.cotenite.agentxkotlin.domain.agent.model.AgentDTO
import cn.cotenite.agentxkotlin.domain.agent.model.AgentVersionDTO
import cn.cotenite.agentxkotlin.domain.agent.model.PublishStatus
import cn.cotenite.agentxkotlin.domain.agent.service.AgentService
import cn.cotenite.agentxkotlin.domain.common.exception.ParamValidationException
import cn.cotenite.agentxkotlin.interfaces.dto.agent.*
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 14:12
 */
@Service
class AgentAppService(
    private val agentService: AgentService
){
    /**
     * 创建新Agent
     */
    fun createAgent(request: CreateAgentRequest, userId: String): AgentDTO {
        // 在应用层验证请求
        request.validate()

        // 使用组装器创建领域实体
        val entity = AgentAssembler.toEntity(request, userId)

        // 调用领域服务
        return agentService.createAgent(entity)
    }

    /**
     * 获取Agent信息
     */
    fun getAgent(agentId: String, userId: String): AgentDTO {
        return agentService.getAgent(agentId, userId)
    }

    /**
     * 获取用户的Agent列表，支持状态和名称过滤
     */
    fun getUserAgents(userId: String, searchAgentsRequest: SearchAgentsRequest): List<AgentDTO> {
        return agentService.getUserAgents(userId, searchAgentsRequest)
    }

    /**
     * 获取已上架的Agent列表，支持名称搜索
     */
    fun getPublishedAgentsByName(searchAgentsRequest: SearchAgentsRequest): List<AgentVersionDTO> {
        return agentService.getPublishedAgentsByName(searchAgentsRequest)
    }

    /**
     * 获取待审核的Agent列表
     */
    fun getPendingReviewAgents(): List<AgentDTO> {
        return agentService.getPendingReviewAgents()
    }

    /**
     * 更新Agent信息（基本信息和配置合并更新）
     */
    fun updateAgent(agentId: String, request: UpdateAgentRequest, userId: String): AgentDTO {
        // 在应用层验证请求
        request.validate()

        // 使用组装器创建更新实体
        val updateEntity = AgentAssembler.toEntity(request, userId)
        // 调用领域服务更新Agent
        return agentService.updateAgent(agentId, updateEntity)
    }

    /**
     * 切换Agent的启用/禁用状态
     */
    fun toggleAgentStatus(agentId: String): AgentDTO {
        return agentService.toggleAgentStatus(agentId)
    }

    /**
     * 删除Agent
     */
    fun deleteAgent(agentId: String, userId: String) {
        agentService.deleteAgent(agentId, userId)
    }

    /**
     * 发布Agent版本
     */
    fun publishAgentVersion(agentId: String, request: PublishAgentVersionRequest, userId: String): AgentVersionDTO {
        // 在应用层验证请求
        request.validate()

        // 获取当前Agent
        val currentAgentDTO = agentService.getAgent(agentId, userId)

        // 获取最新版本，检查版本号大小
        val latestVersion = agentService.getLatestAgentVersion(agentId)
        if (latestVersion != null) {
            // 检查版本号是否大于上一个版本
            if (!request.isVersionGreaterThan(latestVersion.versionNumber)) {
                throw ParamValidationException(
                    "versionNumber",
                    "新版本号(" + request.versionNumber +
                            ")必须大于当前最新版本号(" + latestVersion.versionNumber + ")"
                )
            }
        }

        // 使用组装器创建版本实体
        val versionEntity = AgentAssembler.createVersionEntity(currentAgentDTO.toEntity(), request)

        versionEntity.userId = userId
        // 调用领域服务发布版本
        return agentService.publishAgentVersion(agentId, versionEntity)
    }

    /**
     * 获取Agent的所有版本
     */
    fun getAgentVersions(agentId: String, userId: String): List<AgentVersionDTO> {
        return agentService.getAgentVersions(agentId, userId)
    }

    /**
     * 获取Agent的特定版本
     */
    fun getAgentVersion(agentId: String, versionNumber: String): AgentVersionDTO {
        return agentService.getAgentVersion(agentId, versionNumber)
    }

    /**
     * 获取Agent的最新版本
     */
    fun getLatestAgentVersion(agentId: String): AgentVersionDTO {
        return agentService.getLatestAgentVersion(agentId)
    }

    /**
     * 审核Agent版本
     */
    fun reviewAgentVersion(versionId: String, request: ReviewAgentVersionRequest): AgentVersionDTO {
        // 在应用层验证请求
        request.validate()

        // 根据状态执行相应操作
        return if (PublishStatus.REJECTED == request.status) {
            // 拒绝发布，需使用拒绝原因
            agentService.rejectVersion(versionId, request.rejectReason!!)
        } else {
            // 其他状态变更，直接更新状态
            agentService.updateVersionPublishStatus(versionId, request.status)
        }
    }

    /**
     * 根据发布状态获取版本列表
     *
     * @param status 发布状态
     * @return 版本列表（每个助理只返回最新版本）
     */
    fun getVersionsByStatus(status: PublishStatus): List<AgentVersionDTO> {
        return agentService.getVersionsByStatus(status)
    }
}
