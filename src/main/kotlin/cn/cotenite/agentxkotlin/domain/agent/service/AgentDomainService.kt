package cn.cotenite.agentxkotlin.domain.agent.service

import cn.cotenite.agentxkotlin.domain.agent.constant.PublishStatus
import cn.cotenite.agentxkotlin.domain.agent.dto.AgentDTO
import cn.cotenite.agentxkotlin.domain.agent.dto.AgentVersionDTO
import cn.cotenite.agentxkotlin.domain.agent.model.*
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentRepository
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentVersionRepository
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.infrastructure.util.ValidationUtils
import cn.cotenite.agentxkotlin.interfaces.dto.agent.SearchAgentsRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 12:05
 */
@Service
class AgentDomainService(
    private val agentRepository: AgentRepository,
    private val agentVersionRepository: AgentVersionRepository
){

    @Transactional
    fun createAgent(entity: AgentEntity): AgentDTO {
        ValidationUtils.notEmpty(entity.name, "name")
        ValidationUtils.notEmpty(entity.userId, "userId")


        agentRepository.save(entity)
        return entity.toDTO()
    }

    fun getAgent(agentId: String, userId: String): AgentDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notEmpty(userId, "userId")

        val agent = agentRepository.findByIdAndUserIdAndDeletedAtIsNull(agentId, userId) ?: throw BusinessException("Agent 不存在:${agentId}")

        return agent.toDTO()
    }

    fun getUserAgents(userId: String, searchAgentsRequest: SearchAgentsRequest): List<AgentDTO> {
        ValidationUtils.notEmpty(userId, "userId")

        val agents = if (searchAgentsRequest.name.isNullOrBlank()) {
            agentRepository.findByUserIdAndDeletedAtIsNullOrderByUpdatedAtDescCreatedAtDesc(userId)
        } else {
            agentRepository.findByUserIdAndNameContainingIgnoreCaseAndDeletedAtIsNullOrderByUpdatedAtDescCreatedAtDesc(userId, searchAgentsRequest.name!!)
        }
        return agents.map { obj: AgentEntity -> obj.toDTO() }
    }

    fun getPublishedAgentsByName(searchAgentsRequest: SearchAgentsRequest): List<AgentVersionDTO> {
        val latestVersions = agentVersionRepository.findLatestVersionsByNameAndPublishStatus(
            searchAgentsRequest.name,
            PublishStatus.PUBLISHED.code
        )

        return this.combineAgentsWithVersions(latestVersions)
    }

    private fun combineAgentsWithVersions(versionEntities: List<AgentVersionEntity>): List<AgentVersionDTO> {
        if (versionEntities.isEmpty()){
            return Collections.emptyList()
        }
        val agentIds = versionEntities.map { obj: AgentVersionEntity -> obj.agentId }
        val agents = agentRepository.findByIdInAndDeletedAtIsNull(agentIds)

        val agentVersionMap  = versionEntities.associateBy { it.agentId }

        return agents.mapNotNull { agent ->
            val agentVersion = agentVersionMap[agent.id]
            agentVersion?.toDTO()
        }.toList()
    }

    fun getPendingReviewAgents(): List<AgentDTO> {
        val reviewingVersions   = this.getVersionsByStatus(PublishStatus.REVIEWING)

        return reviewingVersions.mapNotNull { version->
            val agent = agentRepository.findByIdOrNull(version.agentId)
            if (agent!=null && agent.enabled){
                agent.toDTO()
            }
            null
        }.toList()
    }

    @Transactional
    fun updateAgent(agentId: String, entity: AgentEntity): AgentDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notNull(entity, "updateEntity")
        ValidationUtils.notEmpty(entity.name, "name")
        ValidationUtils.notEmpty(entity.userId, "userId")

        val existingAgent = agentRepository.findByIdAndUserIdAndDeletedAtIsNull(agentId, entity.userId) ?: throw BusinessException("Agent 不存在:${agentId}")

        existingAgent.name = entity.name
        existingAgent.description = entity.description
        existingAgent.type = entity.type
        existingAgent.tags = entity.tags
        existingAgent.isPublic = entity.isPublic
        existingAgent.updatedAt = entity.updatedAt

        agentRepository.save(existingAgent)
        return existingAgent.toDTO()
    }

    fun toggleAgentStatus(agentId: String): AgentDTO {
        ValidationUtils.notEmpty(agentId, "agentId")

        val agent = agentRepository.findByIdOrNull(agentId) ?: throw BusinessException("Agent 不存在:${agentId}")

        if (agent.enabled){
            agent.disable()
        }else{
            agent.enable()
        }

        agentRepository.save(agent)
        return agent.toDTO()
    }

    @Transactional
    fun deleteAgent(agentId: String, userId: String) {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notEmpty(userId, "userId")

        val now = java.time.LocalDateTime.now()
        agentRepository.softDeleteByIdAndUserId(agentId, userId, now)
        agentVersionRepository.softDeleteByAgentIdAndUserId(agentId, userId, now)
    }

    @Transactional
    fun publishAgentVersion(agentId: String, versionEntity: AgentVersionEntity): AgentVersionDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notNull(versionEntity, "versionEntity")
        ValidationUtils.notEmpty(versionEntity.versionNumber, "versionNumber")
        ValidationUtils.notEmpty(versionEntity.userId, "userId")
        agentRepository.findByIdOrNull(agentId)?:throw BusinessException("Agent不存在: $agentId")

        val latestVersion = agentVersionRepository.findLatestVersionByAgentIdAndUserId(agentId, versionEntity.userId)

        if (latestVersion!=null){
            val newVersion = versionEntity.versionNumber
            val oldVersion = latestVersion.versionNumber

            if (newVersion==oldVersion){
                throw BusinessException("版本号已存在：$newVersion")
            }

            if (!isVersionGreaterThan(newVersion, oldVersion)) {
                throw BusinessException("新版本号($newVersion)必须大于当前最新版本号($oldVersion)")
            }

        }

        versionEntity.agentId=agentId
        versionEntity.publishStatus= PublishStatus.REVIEWING.code

        agentVersionRepository.save(versionEntity)

        return versionEntity.toDTO()
    }

    @Transactional
    fun updateVersionPublishStatus(versionId: String, status: PublishStatus): AgentVersionDTO {
        ValidationUtils.notEmpty(versionId, "versionId")
        ValidationUtils.notNull(status, "status")

        val version = agentVersionRepository.findByIdOrNull(versionId)?:throw BusinessException("版本不存在：$versionId")

        version.rejectReason=""

        version.updatePublishStatus(status)
        agentVersionRepository.save(version)

        if (status== PublishStatus.PUBLISHED){
            val agent = agentRepository.findByIdOrNull(version.agentId)
            if (agent!=null){
                agent.publishVersion(versionId)
                agentRepository.save(agent)
            }
        }
        return version.toDTO()
    }

    @Transactional
    fun rejectVersion(versionId: String, reason: String): AgentVersionDTO {
        ValidationUtils.notEmpty(versionId, "versionId")
        ValidationUtils.notEmpty(reason, "reason")

        val version = agentVersionRepository.findByIdOrNull(versionId) ?: throw BusinessException("版本不存在: $versionId")

        version.reject(reason)
        agentVersionRepository.save(version)

        return version.toDTO()
    }

    private fun isVersionGreaterThan(newVersion: String, oldVersion: String): Boolean {
        if (oldVersion.trim().isEmpty()){
            return true
        }

        val current = newVersion.split("\\.")
        val last  = oldVersion.split("\\.")

        if (current.size!=3||last.size!=3){
            throw BusinessException("版本号必须遵循 x.y.z 格式")
        }

        try {
            val currentMajor = current[0].toInt()
            val lastMajor = last[0].toInt()
            if (currentMajor > lastMajor) return true
            if (currentMajor < lastMajor) return false

            val currentMinor = current[1].toInt()
            val lastMinor = last[1].toInt()
            if (currentMinor > lastMinor) return true
            if (currentMinor < lastMinor) return false

            val currentPatch = current[2].toInt()
            val lastPatch = last[2].toInt()

            return currentPatch > lastPatch
        }catch (e:Exception){
            throw BusinessException("版本号格式错误，必须是数字: ${e.message}")
        }
    }

    fun getAgentVersions(agentId: String, userId: String): List<AgentVersionDTO> {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notEmpty(userId, "userId")
        val versions = agentVersionRepository.findByAgentIdAndUserIdAndDeletedAtIsNullOrderByPublishedAtDesc(agentId, userId)
        return versions.map { obj: AgentVersionEntity -> obj.toDTO() }.toList()
    }

    fun getAgentVersion(agentId: String, versionNumber: String): AgentVersionDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notEmpty(versionNumber, "versionNumber")

        val version = agentVersionRepository.findByAgentIdAndVersionNumberAndDeletedAtIsNull(agentId, versionNumber) ?: throw BusinessException("版本不存在: $versionNumber")

        return version.toDTO()
    }

    fun getLatestAgentVersion(agentId: String): AgentVersionDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        val version = agentVersionRepository.findLatestVersionByAgentId(agentId) ?: throw BusinessException("版本不存在")
        return version.toDTO()
    }

    fun getVersionsByStatus(status: PublishStatus): List<AgentVersionDTO> {
        val latestVersions = agentVersionRepository.findLatestVersionsByPublishStatus(status.code)
        return latestVersions.map { obj: AgentVersionEntity -> obj.toDTO() }.toList()
    }

}
