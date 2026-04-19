package cn.cotenite.domain.llm.service

import cn.cotenite.domain.llm.event.*
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderAggregate
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.domain.llm.model.enums.ProviderType
import cn.cotenite.domain.llm.repository.ModelRepository
import cn.cotenite.domain.llm.repository.ProviderRepository
import cn.cotenite.infrastructure.entity.Operator
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol
import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.core.toolkit.Wrappers
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * LLM领域服务
 */
@Service
class LlmDomainService(
    private val providerRepository: ProviderRepository,
    private val modelRepository: ModelRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    fun createProvider(provider: ProviderEntity): ProviderEntity = provider.apply {
        validateProviderProtocol(protocol)
        providerRepository.insert(this)
    }

    fun updateProvider(provider: ProviderEntity) {
        validateProviderProtocol(provider.protocol)
        providerRepository.checkedUpdate(
            provider,
            KtUpdateWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::id, provider.id)
                .eq(provider.needCheckUserId(), ProviderEntity::userId, provider.userId)
        )
    }

    fun getUserProviders(userId: String): List<ProviderAggregate> =
        providerRepository.selectList(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::userId, userId)
        ).let(::buildProviderAggregatesWithActiveModels)

    fun getAllProviders(userId: String): List<ProviderAggregate> =
        providerRepository.selectList(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::userId, userId)
                .or()
                .eq(ProviderEntity::isOfficial, true)
        ).let(::buildProviderAggregatesWithActiveModels)

    fun getOfficialProviders(): List<ProviderAggregate> =
        providerRepository.selectList(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::isOfficial, true)
        ).let(::buildProviderAggregatesWithActiveModels)

    fun getCustomProviders(userId: String): List<ProviderAggregate> =
        providerRepository.selectList(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::userId, userId)
                .eq(ProviderEntity::isOfficial, false)
        ).let(::buildProviderAggregatesWithActiveModels)

    private fun buildProviderAggregatesWithActiveModels(providers: List<ProviderEntity>): List<ProviderAggregate> =
        providers.takeIf { it.isNotEmpty() }?.let {
            val activeModelsByProviderId = modelRepository.selectList(
                KtQueryWrapper(ModelEntity::class.java)
                    .`in`(ModelEntity::providerId, it.mapNotNull(ProviderEntity::id))
                    .eq(ModelEntity::status, true)
            ).groupBy(ModelEntity::providerId)

            it.map { provider -> ProviderAggregate(provider, activeModelsByProviderId[provider.id]) }
        } ?: emptyList()

    fun getProvider(providerId: String, userId: String): ProviderEntity =
        providerRepository.selectOne(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::id, providerId)
                .eq(ProviderEntity::userId, userId)
        ) ?: throw BusinessException("服务商不存在")

    fun getProvider(providerId: String): ProviderEntity =
        providerRepository.selectOne(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::id, providerId)
        ) ?: throw BusinessException("服务商不存在")

    fun findProviderById(providerId: String): ProviderEntity? = providerRepository.selectById(providerId)

    fun checkProviderExists(providerId: String, userId: String) {
        getProvider(providerId, userId)
    }

    fun getProviderAggregate(providerId: String, userId: String): ProviderAggregate =
        ProviderAggregate(getProvider(providerId, userId), getActiveModelList(providerId, userId))

    fun getModelList(providerId: String, userId: String): List<ModelEntity> =
        modelRepository.selectList(
            KtQueryWrapper(ModelEntity::class.java)
                .eq(ModelEntity::providerId, providerId)
                .eq(ModelEntity::userId, userId)
        )

    fun getActiveModelList(providerId: String, userId: String): List<ModelEntity> =
        modelRepository.selectList(
            KtQueryWrapper(ModelEntity::class.java)
                .eq(ModelEntity::providerId, providerId)
                .eq(ModelEntity::userId, userId)
                .eq(ModelEntity::status, true)
        )

    @Transactional
    fun deleteProvider(providerId: String, userId: String, operator: Operator) {

        val wrapper = KtQueryWrapper(ModelEntity::class.java)
            .eq(ModelEntity::providerId, providerId)

        val modelsToDelete = modelRepository.selectList(wrapper)


        providerRepository.checkedDelete(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::id, providerId)
                .eq(operator.needCheckUserId(), ProviderEntity::userId, userId)
        )
        val delete = modelRepository.delete(
            KtQueryWrapper(ModelEntity::class.java)
                .eq(ModelEntity::providerId, providerId)
        )
        if (delete>0){
            val deleteItems = modelsToDelete
                .map { model ->
                    ModelsBatchDeletedEvent.ModelDeleteItem(model.id!!, model.userId!!)
                }
            eventPublisher.publishEvent(ModelsBatchDeletedEvent(deleteItems,userId))
        }
    }

    private fun validateProviderProtocol(protocol: ProviderProtocol?) {
        if (protocol == null || protocol !in ProviderProtocol.entries) {
            throw BusinessException("不支持的服务商协议类型: $protocol")
        }
    }

    fun getProviderProtocols(): List<ProviderProtocol> = ProviderProtocol.entries

    fun createModel(model: ModelEntity) {
        modelRepository.insert(model)
        // 发布模型创建事件
        eventPublisher.publishEvent(ModelCreatedEvent(model.id!!, model.userId!!, model))

    }

    fun updateModel(model: ModelEntity) {
        modelRepository.checkedUpdate(
            model,
            KtUpdateWrapper(ModelEntity::class.java)
                .eq(ModelEntity::id, model.id)
                .eq(ModelEntity::userId, model.userId)
        )

        // 发布模型更新事件
        eventPublisher.publishEvent(ModelUpdatedEvent(model.id!!, model.userId!!, model))
    }

    fun deleteModel(modelId: String, userId: String, operator: Operator) {
        modelRepository.checkedDelete(
            KtQueryWrapper(ModelEntity::class.java)
                .eq(ModelEntity::id, modelId)
                .eq(operator.needCheckUserId(), ModelEntity::userId, userId)
        )

        // 发布模型删除事件
        eventPublisher.publishEvent(ModelDeletedEvent(modelId, userId))

    }

    fun updateModelStatus(modelId: String, userId: String) {
        // 先获取当前模型信息，用于判断状态变更
        val currentModel = getModelById(modelId)
        val currentStatus = currentModel.status
        val newStatus = !currentStatus // 状态取反

        val updateWrapper = KtUpdateWrapper(ModelEntity::class.java)
            .eq(ModelEntity::id, modelId)
            .eq(ModelEntity::userId, userId)
            .setSql("status = NOT status")

        modelRepository.checkedUpdate(updateWrapper)


        // 获取更新后的模型信息
        val updatedModel = getModelById(modelId)


        // 发布模型状态变更事件
        eventPublisher.publishEvent(ModelStatusChangedEvent(modelId, userId, updatedModel, newStatus, ""))
    }

    fun getProvidersByType(providerType: ProviderType, userId: String): List<ProviderAggregate> =
        when (providerType) {
            ProviderType.OFFICIAL -> getOfficialProviders()
            ProviderType.CUSTOM -> getCustomProviders(userId)
            ProviderType.ALL -> getAllProviders(userId)
        }

    fun updateProviderStatus(providerId: String, userId: String) {
        val provider = providerRepository.selectOne(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::id, providerId)
                .eq(ProviderEntity::userId, userId)
        ) ?: throw BusinessException("服务商不存在")

        provider.status = !provider.status
        providerRepository.checkedUpdateById(provider)
    }

    fun getModelById(modelId: String): ModelEntity =
        modelRepository.selectById(modelId) ?: throw BusinessException("模型不存在")

    /** 获取所有激活的模型
     * @return 所有激活的模型列表
     */
    fun getAllActiveModels(): MutableList<ModelEntity?> {
        val wrapper= KtQueryWrapper (ModelEntity::class.java).eq(ModelEntity::status, true)
        return modelRepository.selectList(wrapper)
    }

}
