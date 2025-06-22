package cn.cotenite.agentxkotlin.domain.agent.service

import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.domain.agent.model.AgentEntity
import cn.cotenite.agentxkotlin.domain.agent.model.AgentVersionEntity
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentRepository
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentVersionRepository
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentWorkspaceRepository
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Agent服务实现类
 */
@Service
class AgentDomainService(
    private val agentRepository: AgentRepository,
    private val agentVersionRepository: AgentVersionRepository,
    private val agentWorkspaceRepository: AgentWorkspaceRepository
) {

    /**
     * 创建新Agent
     */
    @Transactional
    fun createAgent(agent: AgentEntity): AgentEntity {
        try {
            return agentRepository.save(agent)
        } catch (e: Exception) {
            throw BusinessException("创建助理失败: ${e.message}")
        }
    }

    /**
     * 获取单个Agent信息
     */
    fun getAgent(agentId: String, userId: String): AgentEntity {
        return agentRepository.findByIdAndUserId(agentId, userId)
            ?: throw BusinessException("助理不存在")
    }

    /**
     * 获取用户的Agent列表，支持状态和名称过滤
     */
    fun getUserAgents(userId: String, agent: AgentEntity): List<AgentEntity> {
        return if (agent.name.isNullOrBlank()) {
            agentRepository.findByUserIdOrderByUpdatedAtDesc(userId)
        } else {
            agentRepository.findByUserIdAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(userId, agent.name)
        }
    }

    /**
     * 获取已上架的Agent列表，支持名称搜索
     * 当name为空时返回所有已上架Agent
     */
    fun getPublishedAgentsByName(agent: AgentEntity): List<AgentVersionEntity> {
        // 使用带名称和状态条件的查询
        val latestVersions = agentVersionRepository.findLatestVersionsByNameAndStatus(
            agent.name,
            PublishStatus.PUBLISHED.code.toString()
        )

        // 组合助理和版本信息
        return combineAgentsWithVersions(latestVersions)
    }

    /**
     * 更新Agent信息（基本信息和配置合并更新）
     */
    @Transactional
    fun updateAgent(updateEntity: AgentEntity): AgentEntity {
        try {
            // 检查Agent是否存在且属于该用户
            val existingAgent = agentRepository.findByIdAndUserId(updateEntity.id, updateEntity.userId)
                ?: throw BusinessException("助理不存在或无权限")
            
            // 更新实体
            return agentRepository.save(updateEntity)
        } catch (e: Exception) {
            throw BusinessException("更新助理失败: ${e.message}")
        }
    }

    /**
     * 切换Agent的启用/禁用状态
     */
    @Transactional
    fun toggleAgentStatus(agentId: String): AgentEntity {
        try {
            val agent = agentRepository.findById(agentId).orElse(null)
                ?: throw BusinessException("助理不存在: $agentId")

            // 根据当前状态切换
            if (agent.enabled) {
                agent.disable()
            } else {
                agent.enable()
            }

            return agentRepository.save(agent)
        } catch (e: Exception) {
            throw BusinessException("更新助理状态失败: ${e.message}")
        }
    }

    /**
     * 删除Agent
     */
    @Transactional
    fun deleteAgent(agentId: String, userId: String) {
        try {
            // 检查Agent是否存在且属于该用户
            val agent = agentRepository.findByIdAndUserId(agentId, userId)
                ?: throw BusinessException("助理不存在或无权限")
            
            // 删除Agent
            agentRepository.delete(agent)
            
            // 删除版本
            agentVersionRepository.deleteByAgentIdAndUserId(agentId, userId)
        } catch (e: Exception) {
            throw BusinessException("删除助理失败: ${e.message}")
        }
    }

    /**
     * 发布Agent版本
     */
    @Transactional
    fun publishAgentVersion(agentId: String, versionEntity: AgentVersionEntity): AgentVersionEntity {
        try {
            val agent = agentRepository.findById(agentId).orElse(null)
                ?: throw BusinessException("助理不存在: $agentId")

            // 查询最新版本号进行比较
            val latestVersion = agentVersionRepository.findAll()
                .filter { it.agentId == agentId && it.userId == versionEntity.userId }
                .maxByOrNull { it.publishedAt ?: java.time.LocalDateTime.MIN }

            latestVersion?.let {
                // 版本号比较
                val newVersion = versionEntity.versionNumber?:throw BusinessException("新版本号不存在")
                val oldVersion = it.versionNumber?:throw BusinessException("旧版本号不存在")

                // 检查是否为相同版本号
                if (newVersion == oldVersion) {
                    throw BusinessException("版本号已存在: $newVersion")
                }

                // 检查新版本号是否大于旧版本号
                if (!isVersionGreaterThan(newVersion, oldVersion)) {
                    throw BusinessException("新版本号($newVersion)必须大于当前最新版本号($oldVersion)")
                }
            }

            // 设置版本关联的Agent ID
            versionEntity.agentId = agentId

            // 设置版本状态为审核中
            versionEntity.publishStatus = PublishStatus.REVIEWING.code

            // 保存版本
            return agentVersionRepository.save(versionEntity)
        } catch (e: Exception) {
            throw BusinessException("发布版本失败: ${e.message}")
        }
    }

    /**
     * 更新版本发布状态
     */
    @Transactional
    fun updateVersionPublishStatus(versionId: String, status: PublishStatus): AgentVersionEntity {
        try {
            val version = agentVersionRepository.findById(versionId).orElse(null)
                ?: throw BusinessException("版本不存在: $versionId")

            version.rejectReason = ""

            // 更新版本状态
            version.updatePublishStatus(status)
            agentVersionRepository.save(version)

            // 如果状态更新为已发布，则绑定为Agent的publishedVersion
            if (status == PublishStatus.PUBLISHED) {
                val agent = version.agentId?.let { agentRepository.findById(it) }?.orElse(null)
                agent?.let {
                    it.publishVersion(versionId)
                    agentRepository.save(it)
                }
            }

            return version
        } catch (e: Exception) {
            throw BusinessException("更新版本状态失败: ${e.message}")
        }
    }

    /**
     * 拒绝版本发布
     */
    @Transactional
    fun rejectVersion(versionId: String, reason: String): AgentVersionEntity {
        try {
            val version = agentVersionRepository.findById(versionId).orElse(null)
                ?: throw BusinessException("版本不存在: $versionId")

            // 拒绝版本发布
            version.reject(reason)
            return agentVersionRepository.save(version)
        } catch (e: Exception) {
            throw BusinessException("拒绝版本失败: ${e.message}")
        }
    }

    /**
     * 获取Agent的所有版本
     */
    fun getAgentVersions(agentId: String, userId: String): List<AgentVersionEntity> {
        // 查询Agent
        val agent = agentRepository.findById(agentId).orElse(null)
        if (agent == null || agent.userId != userId) {
            throw BusinessException("助理不存在或无权访问")
        }

        // 查询所有版本并按创建时间降序排序
        return agentVersionRepository.findAll()
            .filter { it.agentId == agentId }
            .sortedByDescending { it.createdAt }
    }

    /**
     * 获取Agent的特定版本
     */
    fun getAgentVersion(agentId: String, versionNumber: String): AgentVersionEntity {
        return agentVersionRepository.findByAgentIdAndVersionNumber(agentId, versionNumber)
            ?: throw BusinessException("助理版本不存在: $versionNumber")
    }

    /**
     * 获取Agent的最新版本
     */
    fun getLatestAgentVersion(agentId: String): AgentVersionEntity? {
        return agentVersionRepository.findAll()
            .filter { it.agentId == agentId }
            .maxByOrNull { it.publishedAt ?: java.time.LocalDateTime.MIN }
    }

    /**
     * 获取指定状态的所有版本
     * 注：只返回每个助理的最新版本，避免同一助理多个版本同时出现
     */
    fun getVersionsByStatus(status: PublishStatus?): List<AgentVersionEntity> {
        // 直接通过SQL查询每个agentId的最新版本
        return agentVersionRepository.findLatestVersionsByStatus(status?.code?.toString())
    }

    /**
     * 校验 agent 是否存在
     */
    fun exist(agentId: String, userId: String): Boolean {
        return agentRepository.existsByIdAndUserId(agentId, userId)
    }

    /**
     * 根据 agentIds 获取 agents
     */
    fun getAgentsByIds(agentIds: List<String>): List<AgentEntity> {
        return agentRepository.findByIdIn(agentIds)
    }

    fun getAgentById(agentId: String): AgentEntity {
        return getAgentsByIds(listOf(agentId)).firstOrNull()
            ?: throw BusinessException("助理不存在")
    }

    fun getAgentWithPermissionCheck(agentId: String, userId: String): AgentEntity {
        // 检查工作区是否存在
        val b1 = agentWorkspaceRepository.existsByAgentIdAndUserId(agentId, userId)
        val b2 = exist(agentId, userId)
        
        if (!b1 && !b2) {
            throw BusinessException("助理不存在")
        }
        
        val agentEntity = getAgentById(agentId)

        // 如果有版本则使用版本
        agentEntity.publishedVersion?.let { publishedVersion ->
            if (publishedVersion.isNotBlank()) {
                val agentVersionEntity = getAgentVersionById(publishedVersion)
                BeanUtils.copyProperties(agentVersionEntity, agentEntity)
            }
        }

        return agentEntity
    }

    fun getAgentVersionById(versionId: String): AgentVersionEntity {
        return agentVersionRepository.findById(versionId).orElse(null)
            ?: throw BusinessException("版本不存在: $versionId")
    }

    /**
     * 组合助理和版本信息
     *
     * @param versionEntities 版本实体列表
     * @return 组合后的版本AgentVersionEntity列表
     */
    private fun combineAgentsWithVersions(versionEntities: List<AgentVersionEntity>): List<AgentVersionEntity> {
        // 如果版本列表为空，直接返回空列表
        if (versionEntities.isEmpty()) {
            return emptyList()
        }

        // 根据版本中的 agent_id 以及 enable == true 查出对应的 agents
        val agentIds = versionEntities.map { it.agentId }
        val agents = agentRepository.findByIdInAndEnabled(agentIds, true)

        // 将版本转为 map，key：agent_id，value：本身
        val agentVersionMap = versionEntities.associateBy { it.agentId }

        return agents.mapNotNull { agent ->
            agentVersionMap[agent.id]
        }
    }

    /**
     * 比较版本号大小
     *
     * @param newVersion 新版本号
     * @param oldVersion 旧版本号
     * @return 如果新版本大于旧版本返回true，否则返回false
     */
    private fun isVersionGreaterThan(newVersion: String, oldVersion: String): Boolean {
        if (oldVersion.isBlank()) {
            return true // 如果没有旧版本，新版本肯定更大
        }

        // 分割版本号
        val current = newVersion.split(".")
        val last = oldVersion.split(".")

        // 确保版本号格式正确
        if (current.size != 3 || last.size != 3) {
            throw BusinessException("版本号必须遵循 x.y.z 格式")
        }

        try {
            // 比较主版本号
            val currentMajor = current[0].toInt()
            val lastMajor = last[0].toInt()
            if (currentMajor > lastMajor) return true
            if (currentMajor < lastMajor) return false

            // 主版本号相同，比较次版本号
            val currentMinor = current[1].toInt()
            val lastMinor = last[1].toInt()
            if (currentMinor > lastMinor) return true
            if (currentMinor < lastMinor) return false

            // 主版本号和次版本号都相同，比较修订版本号
            val currentPatch = current[2].toInt()
            val lastPatch = last[2].toInt()

            return currentPatch > lastPatch
        } catch (e: NumberFormatException) {
            throw BusinessException("版本号格式错误，必须是数字: ${e.message}")
        }
    }

}