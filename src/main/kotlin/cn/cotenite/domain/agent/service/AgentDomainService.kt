package cn.cotenite.domain.agent.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import cn.cotenite.domain.agent.constant.PublishStatus
import cn.cotenite.domain.agent.model.AgentEntity
import cn.cotenite.domain.agent.model.AgentVersionEntity
import cn.cotenite.domain.agent.repository.AgentRepository
import cn.cotenite.domain.agent.repository.AgentVersionRepository
import cn.cotenite.domain.agent.repository.AgentWorkspaceRepository
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * Agent领域服务
 */
@Service
class AgentDomainService(
    private val agentRepository: AgentRepository,
    private val agentVersionRepository: AgentVersionRepository,
    private val agentWorkspaceRepository: AgentWorkspaceRepository
) {

    @Transactional
    fun createAgent(agent: AgentEntity): AgentEntity = agent.apply { agentRepository.insert(this) }

    fun getAgent(agentId: String, userId: String): AgentEntity =
        agentRepository.selectOne(
            KtQueryWrapper(AgentEntity::class.java)
                .eq(AgentEntity::id, agentId)
                .eq(AgentEntity::userId, userId)
        ) ?: throw BusinessException("Agent不存在: $agentId")

    fun getUserAgents(userId: String, queryAgent: AgentEntity): List<AgentEntity> =
        agentRepository.selectList(
            KtQueryWrapper(AgentEntity::class.java)
                .eq(AgentEntity::userId, userId)
                .like(!queryAgent.name.isNullOrBlank(), AgentEntity::name, queryAgent.name)
                .orderByDesc(AgentEntity::updatedAt)
        )

    fun getPublishedAgentsByName(agent: AgentEntity): List<AgentVersionEntity> =
        agentVersionRepository
            .selectLatestVersionsByNameAndStatus(agent.name, PublishStatus.PUBLISHED.code)
            .let(::combineAgentsWithVersions)

    @Transactional
    fun updateAgent(updateEntity: AgentEntity): AgentEntity {
        val affected = agentRepository.update(
            updateEntity,
            KtUpdateWrapper(AgentEntity::class.java)
                .eq(AgentEntity::id, updateEntity.id)
                .eq(AgentEntity::userId, updateEntity.userId)
        )
        if (affected == 0) throw BusinessException("更新助理失败")
        return updateEntity
    }

    @Transactional
    fun toggleAgentStatus(agentId: String): AgentEntity =
        agentRepository.selectById(agentId)
            ?.apply {
                if (enabled) disable() else enable()
                agentRepository.checkedUpdateById(this)
            }
            ?: throw BusinessException("Agent不存在: $agentId")

    @Transactional
    fun deleteAgent(agentId: String, userId: String) {
        agentRepository.checkedDelete(
            KtQueryWrapper(AgentEntity::class.java)
                .eq(AgentEntity::id, agentId)
                .eq(AgentEntity::userId, userId)
        )
        agentVersionRepository.delete(
            KtQueryWrapper(AgentVersionEntity::class.java)
                .eq(AgentVersionEntity::agentId, agentId)
                .eq(AgentVersionEntity::userId, userId)
        )
    }

    @Transactional
    fun publishAgentVersion(agentId: String, versionEntity: AgentVersionEntity): AgentVersionEntity {
        agentRepository.selectById(agentId) ?: throw BusinessException("Agent不存在: $agentId")

        agentVersionRepository.selectOne(
            KtQueryWrapper(AgentVersionEntity::class.java)
                .eq(AgentVersionEntity::agentId, agentId)
                .eq(AgentVersionEntity::userId, versionEntity.userId)
                .orderByDesc(AgentVersionEntity::publishedAt)
                .last("LIMIT 1")
        )?.let { latestVersion ->
            val newVersion = versionEntity.versionNumber ?: throw BusinessException("版本号不能为空")
            val oldVersion = latestVersion.versionNumber.orEmpty()
            when {
                newVersion == oldVersion -> throw BusinessException("版本号已存在: $newVersion")
                !isVersionGreaterThan(newVersion, oldVersion) -> {
                    throw BusinessException("新版本号($newVersion)必须大于当前最新版本号($oldVersion)")
                }
            }
        }

        return versionEntity.apply {
            this.agentId = agentId
            publishStatus = PublishStatus.REVIEWING.code
            agentVersionRepository.insert(this)
        }
    }

    @Transactional
    fun updateVersionPublishStatus(versionId: String, status: PublishStatus): AgentVersionEntity {
        val version = agentVersionRepository.selectById(versionId) ?: throw BusinessException("版本不存在: $versionId")

        version.apply {
            rejectReason = ""
            updatePublishStatus(status)
        }
        agentVersionRepository.checkedUpdateById(version)

        if (status == PublishStatus.PUBLISHED) {
            agentRepository.selectById(version.agentId)?.let { agent ->
                agent.publishVersion(versionId)
                agentRepository.checkedUpdateById(agent)
            }
        }

        return version
    }

    @Transactional
    fun rejectVersion(versionId: String, reason: String): AgentVersionEntity =
        agentVersionRepository.selectById(versionId)
            ?.apply {
                reject(reason)
                agentVersionRepository.checkedUpdateById(this)
            }
            ?: throw BusinessException("版本不存在: $versionId")

    fun getAgentVersions(agentId: String, userId: String): List<AgentVersionEntity> {
        val agent = agentRepository.selectById(agentId)
        if (agent == null || agent.userId != userId) throw BusinessException("Agent不存在或无权访问")

        return agentVersionRepository.selectList(
            KtQueryWrapper(AgentVersionEntity::class.java)
                .eq(AgentVersionEntity::agentId, agentId)
                .orderByDesc(AgentVersionEntity::createdAt)
        )
    }

    fun getAgentVersion(agentId: String, versionNumber: String): AgentVersionEntity =
        agentVersionRepository.selectOne(
            KtQueryWrapper(AgentVersionEntity::class.java)
                .eq(AgentVersionEntity::agentId, agentId)
                .eq(AgentVersionEntity::versionNumber, versionNumber)
        ) ?: throw BusinessException("Agent版本不存在: $versionNumber")

    fun getLatestAgentVersion(agentId: String): AgentVersionEntity? =
        agentVersionRepository.selectOne(
            KtQueryWrapper(AgentVersionEntity::class.java)
                .eq(AgentVersionEntity::agentId, agentId)
                .orderByDesc(AgentVersionEntity::publishedAt)
                .last("LIMIT 1")
        )

    fun getVersionsByStatus(status: PublishStatus?): List<AgentVersionEntity> =
        agentVersionRepository.selectLatestVersionsByStatus(status?.code)

    fun exist(agentId: String, userId: String): Boolean =
        agentRepository.selectOne(
            KtQueryWrapper(AgentEntity::class.java)
                .eq(AgentEntity::id, agentId)
                .eq(AgentEntity::userId, userId)
        ) != null

    fun getAgentsByIds(agentIds: List<String>): List<AgentEntity> = agentRepository.selectByIds(agentIds)

    fun getAgentById(agentId: String): AgentEntity =
        getAgentsByIds(listOf(agentId)).firstOrNull() ?: throw BusinessException("助理不存在")

    fun getAgentWithPermissionCheck(agentId: String, userId: String): AgentEntity {
        val inWorkspace = agentWorkspaceRepository.exist(agentId, userId)
        val ownAgent = exist(agentId, userId)
        if (!inWorkspace && !ownAgent) throw BusinessException("助理不存在")

        return getAgentById(agentId).apply {
            publishedVersion
                ?.takeIf { it.isNotBlank() }
                ?.let(::getAgentVersionById)
                ?.let { versionEntity ->
                    name = versionEntity.name
                    avatar = versionEntity.avatar
                    description = versionEntity.description
                    systemPrompt = versionEntity.systemPrompt
                    welcomeMessage = versionEntity.welcomeMessage
                    toolIds = versionEntity.toolIds
                    knowledgeBaseIds = versionEntity.knowledgeBaseIds
                    agentType = versionEntity.agentType ?: agentType
                }
        }
    }

    fun getAgentVersionById(versionId: String): AgentVersionEntity? = agentVersionRepository.selectById(versionId)

    private fun combineAgentsWithVersions(versionEntities: List<AgentVersionEntity>): List<AgentVersionEntity> =
        versionEntities
            .takeIf { it.isNotEmpty() }
            ?.let {
                val versionMap = it.associateBy(AgentVersionEntity::agentId)
                agentRepository.selectList(
                    KtQueryWrapper(AgentEntity::class.java)
                        .`in`(AgentEntity::id, it.mapNotNull(AgentVersionEntity::agentId))
                        .eq(AgentEntity::enabled, true)
                ).mapNotNull { agent -> versionMap[agent.id] }
            }
            ?: emptyList()

    private fun isVersionGreaterThan(newVersion: String, oldVersion: String?): Boolean {
        if (oldVersion.isNullOrBlank()) return true

        val current = newVersion.split(".")
        val last = oldVersion.split(".")
        if (current.size != 3 || last.size != 3) throw BusinessException("版本号必须遵循 x.y.z 格式")

        return try {
            val (curMajor, curMinor, curPatch) = current.map(String::toInt)
            val (lastMajor, lastMinor, lastPatch) = last.map(String::toInt)

            when {
                curMajor != lastMajor -> curMajor > lastMajor
                curMinor != lastMinor -> curMinor > lastMinor
                else -> curPatch > lastPatch
            }
        } catch (e: NumberFormatException) {
            throw BusinessException("版本号格式错误，必须是数字: ${e.message}")
        }
    }
}
