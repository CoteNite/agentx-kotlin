package cn.cotenite.agentxkotlin.domain.agent.service

import cn.cotenite.agentxkotlin.domain.agent.model.*
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentRepository
import cn.cotenite.agentxkotlin.domain.agent.repository.AgentVersionRepository
import cn.cotenite.agentxkotlin.domain.common.exception.BusinessException
import cn.cotenite.agentxkotlin.domain.common.util.ValidationUtils
import cn.cotenite.agentxkotlin.interfaces.dto.agent.SearchAgentsRequest
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.toolkit.StringUtils
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Collectors


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 12:05
 */
interface AgentService {

    /**
     * 创建新Agent
     *
     * @param entity Agent实体对象
     * @return 创建的Agent信息
     */
    fun createAgent(entity: AgentEntity):AgentDTO

    /**
     * 获取单个Agent信息
     *
     * @param agentId Agent ID，不能为空
     * @return Agent信息
     */
    fun getAgent(agentId: String, userId: String): AgentDTO

    /**
     * 获取用户的Agent列表，支持状态和名称过滤
     *
     * @param userId              用户ID，不能为空
     * @param searchAgentsRequest 查询条件
     * @return 符合条件的Agent列表
     */
    fun getUserAgents(userId: String, searchAgentsRequest: SearchAgentsRequest): List<AgentDTO>

    /**
     * 获取已上架的Agent列表，支持名称搜索
     * 当name为空时返回所有已上架Agent
     */
    fun getPublishedAgentsByName(searchAgentsRequest: SearchAgentsRequest): List<AgentVersionDTO>

    /**
     * 获取待审核的Agent列表
     *
     * @return 待审核的Agent列表
     */
    fun getPendingReviewAgents(): List<AgentDTO>

    /**
     * 更新Agent信息
     *
     * @param agentId Agent ID，不能为空
     * @param entity  更新的Agent实体对象
     * @return 更新后的Agent信息
     */
    fun updateAgent(agentId: String, entity: AgentEntity): AgentDTO

    /**
     * 切换Agent的启用/禁用状态
     *
     * @param agentId Agent ID，不能为空
     * @return 更新后的Agent信息
     */
    fun toggleAgentStatus(agentId: String): AgentDTO

    /**
     * 删除Agent
     *
     * @param agentId Agent ID，不能为空
     * @param userId  用户ID，不能为空
     */
    fun deleteAgent(agentId: String, userId: String)

    /**
     * 发布Agent版本
     *
     * @param agentId       Agent ID，不能为空
     * @param versionEntity 版本实体对象
     * @return 发布的版本信息
     */
    fun publishAgentVersion(agentId: String, versionEntity: AgentVersionEntity): AgentVersionDTO

    /**
     * 更新版本发布状态
     *
     * @param versionId 版本ID，不能为空
     * @param status    发布状态，不能为空
     * @return 更新后的版本信息
     */
    fun updateVersionPublishStatus(versionId: String, status: PublishStatus): AgentVersionDTO

    /**
     * 拒绝版本发布
     *
     * @param versionId 版本ID，不能为空
     * @param reason    拒绝原因，不能为空
     * @return 更新后的版本信息
     */
    fun rejectVersion(versionId: String, reason: String): AgentVersionDTO

    /**
     * 获取Agent的所有版本
     *
     * @param agentId Agent ID，不能为空
     * @param userId  用户ID，不能为空
     * @return 版本列表
     */
    fun getAgentVersions(agentId: String, userId: String): List<AgentVersionDTO>

    /**
     * 获取Agent的特定版本
     *
     * @param agentId       Agent ID，不能为空
     * @param versionNumber 版本号，不能为空
     * @return 版本信息
     */
    fun getAgentVersion(agentId: String, versionNumber: String): AgentVersionDTO

    /**
     * 获取Agent的最新版本
     *
     * @param agentId Agent ID，不能为空
     * @return 最新版本信息
     */
    fun getLatestAgentVersion(agentId: String): AgentVersionDTO

    /**
     * 获取指定状态的所有版本
     *
     * @param status 版本状态，不能为空
     * @return 符合状态的版本列表
     */
    fun getVersionsByStatus(status: PublishStatus): List<AgentVersionDTO>

}

@Service
class AgentServiceImpl(
    private val agentRepository: AgentRepository,
    private val agentVersionRepository: AgentVersionRepository
):AgentService{

    @Transactional
    override fun createAgent(entity: AgentEntity): AgentDTO {
        ValidationUtils.notEmpty(entity.name, "name")
        ValidationUtils.notEmpty(entity.userId, "userId")

        agentRepository.insert(entity)
        return entity.toDTO()
    }

    override fun getAgent(agentId: String, userId: String): AgentDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notEmpty(userId, "userId")

        val wrapper = Wrappers.lambdaQuery<AgentEntity>()
            .eq(AgentEntity::id, agentId)
            .eq(AgentEntity::userId, userId)

        val agent = agentRepository.selectOne(wrapper) ?: throw BusinessException("Agent 不存在:${agentId}")

        return agent.toDTO()
    }

    override fun getUserAgents(userId: String, searchAgentsRequest: SearchAgentsRequest): List<AgentDTO> {
        ValidationUtils.notEmpty(userId, "userId")

        val wrapper = Wrappers.lambdaQuery<AgentEntity>()
            .eq(AgentEntity::userId, userId)
            .like(!StringUtils.isEmpty(searchAgentsRequest.name), AgentEntity::name, searchAgentsRequest.name)
            .orderByDesc(AgentEntity::updatedAt ,AgentEntity::createdAt)

        val agents = agentRepository.selectList(wrapper)
        return agents.map { obj: AgentEntity -> obj.toDTO() }
    }

    override fun getPublishedAgentsByName(searchAgentsRequest: SearchAgentsRequest): List<AgentVersionDTO> {
        val latestVersions = agentVersionRepository.selectLatestVersionsByNameAndStatus(
            searchAgentsRequest.name,
            PublishStatus.PUBLISHED.code
        )

        return this.combineAgentsWithVersions(latestVersions)
    }

    private fun combineAgentsWithVersions(versionEntities: List<AgentVersionEntity>): List<AgentVersionDTO> {
        if (versionEntities.isEmpty()){
            return Collections.emptyList()
        }
        val wrapper = Wrappers.lambdaQuery<AgentEntity>()
            .`in`(AgentEntity::id, versionEntities.map { obj: AgentVersionEntity -> obj.agentId })
            .eq(AgentEntity::id, versionEntities.map { obj: AgentVersionEntity -> obj.agentId })
        val agents = agentRepository.selectList(wrapper)

        val agentVersionMap  = versionEntities.associateBy { it.agentId }

        return agents.mapNotNull { agent ->
            val agentVersion = agentVersionMap[agent.id]
            agentVersion?.toDTO()
        }.toList()
    }

    override fun getPendingReviewAgents(): List<AgentDTO> {
        val reviewingVersions   = this.getVersionsByStatus(PublishStatus.REVIEWING)

        return reviewingVersions.mapNotNull { version->
            val agent = agentRepository.selectById(version.agentId)
            if (agent!=null && agent.enabled){
                agent.toDTO()
            }
            null
        }.toList()
    }

    @Transactional
    override fun updateAgent(agentId: String, entity: AgentEntity): AgentDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notNull(entity, "updateEntity")
        ValidationUtils.notEmpty(entity.name, "name")
        ValidationUtils.notEmpty(entity.userId, "userId")

        val wrapper = Wrappers.lambdaUpdate<AgentEntity>()
            .eq(AgentEntity::id, agentId)
            .eq(AgentEntity::userId, entity.userId)

        agentRepository.update(entity, wrapper)
        return entity.toDTO()
    }

    override fun toggleAgentStatus(agentId: String): AgentDTO {
        ValidationUtils.notEmpty(agentId, "agentId")

        val agent = agentRepository.selectById(agentId)

        if (agent.enabled){
            agent.disable()
        }else{
            agent.enable()
        }

        agentRepository.updateById(agent)
        return agent.toDTO()
    }

    @Transactional
    override fun deleteAgent(agentId: String, userId: String) {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notEmpty(userId, "userId")

        val wrapper = Wrappers.lambdaQuery<AgentEntity>()
            .eq(AgentEntity::id, agentId)
            .eq(AgentEntity::userId, userId)
        agentRepository.delete(wrapper)

        agentVersionRepository.delete(
            Wrappers.lambdaQuery<AgentVersionEntity>()
                .eq(AgentVersionEntity::agentId, agentId)
                .eq(AgentVersionEntity::userId, userId)
        )
    }

    @Transactional
    override fun publishAgentVersion(agentId: String, versionEntity: AgentVersionEntity): AgentVersionDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notNull(versionEntity, "versionEntity")
        ValidationUtils.notEmpty(versionEntity.versionNumber, "versionNumber")
        ValidationUtils.notEmpty(versionEntity.userId, "userId")
        agentRepository.selectById(agentId)?:throw BusinessException("Agent不存在: $agentId")

        val latestVersionQuery: LambdaQueryWrapper<AgentVersionEntity> = Wrappers.lambdaQuery<AgentVersionEntity>()
            .eq(AgentVersionEntity::agentId, agentId)
            .eq(AgentVersionEntity::userId, versionEntity.userId)
            .orderByDesc(AgentVersionEntity::publishedAt as SFunction<AgentVersionEntity, *>)
            .last("LIMIT 1")

        val latestVersion = agentVersionRepository.selectOne(latestVersionQuery)

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
        versionEntity.publishStatus=PublishStatus.REVIEWING.code

        agentVersionRepository.insert(versionEntity)

        return versionEntity.toDTO()
    }

    @Transactional
    override fun updateVersionPublishStatus(versionId: String, status: PublishStatus): AgentVersionDTO {
        ValidationUtils.notEmpty(versionId, "versionId")
        ValidationUtils.notNull(status, "status")

        val version = agentVersionRepository.selectById(versionId)?:throw BusinessException("版本不存在：$versionId")

        version.rejectReason=""

        version.updatePublishStatus(status)
        agentVersionRepository.updateById(version)

        if (status==PublishStatus.PUBLISHED){
            val agent = agentRepository.selectById(version.agentId)
            if (agent!=null){
                agent.publishVersion(versionId)
                agentRepository.updateById(agent)
            }
        }
        return version.toDTO()
    }

    @Transactional
    override fun rejectVersion(versionId: String, reason: String): AgentVersionDTO {
        ValidationUtils.notEmpty(versionId, "versionId")
        ValidationUtils.notEmpty(reason, "reason")

        val version = agentVersionRepository.selectById(versionId) ?: throw BusinessException("版本不存在: $versionId")

        version.reject(reason)
        agentVersionRepository.updateById(version)

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

    override fun getAgentVersions(agentId: String, userId: String): List<AgentVersionDTO> {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notEmpty(userId, "userId")
        val queryWrapper: LambdaQueryWrapper<AgentVersionEntity> = Wrappers.lambdaQuery<AgentVersionEntity>()
            .eq(AgentVersionEntity::agentId, agentId)
            .eq(AgentVersionEntity::userId, userId)
            .orderByDesc(AgentVersionEntity::publishedAt as SFunction<AgentVersionEntity,*>)

        val versions = agentVersionRepository.selectList(queryWrapper)
        return versions.map { obj: AgentVersionEntity -> obj.toDTO() }.toList()
    }

    override fun getAgentVersion(agentId: String, versionNumber: String): AgentVersionDTO {
        ValidationUtils.notEmpty(agentId, "agentId")
        ValidationUtils.notEmpty(versionNumber, "versionNumber")

        val queryWrapper = Wrappers.lambdaQuery<AgentVersionEntity>()
            .eq(AgentVersionEntity::agentId, agentId)
            .eq(AgentVersionEntity::versionNumber, versionNumber)

        val version =
            agentVersionRepository.selectOne(queryWrapper) ?: throw BusinessException("版本不存在: $versionNumber")

        return version.toDTO()
    }

    override fun getLatestAgentVersion(agentId: String): AgentVersionDTO {

        ValidationUtils.notEmpty(agentId, "agentId")

        val queryWrapper: LambdaQueryWrapper<AgentVersionEntity> = Wrappers.lambdaQuery<AgentVersionEntity>()
            .eq(AgentVersionEntity::agentId, agentId)
            .orderByDesc(AgentVersionEntity::publishedAt as SFunction<AgentVersionEntity,*>)
            .last("LIMIT 1")

        val version = agentVersionRepository.selectOne(queryWrapper) ?: throw BusinessException("版本不存在")

        return version.toDTO()
    }

    override fun getVersionsByStatus(status: PublishStatus): List<AgentVersionDTO> {

        val latestVersions = agentVersionRepository.selectLatestVersionsByStatus(status.code)

        return latestVersions.map { obj: AgentVersionEntity -> obj.toDTO() }.toList()
    }

}
