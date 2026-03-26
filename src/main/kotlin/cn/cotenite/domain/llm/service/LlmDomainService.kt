package cn.cotenite.domain.llm.service

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderAggregate
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.domain.llm.model.enums.ProviderType
import cn.cotenite.domain.llm.repository.ModelRepository
import cn.cotenite.domain.llm.repository.ProviderRepository
import cn.cotenite.infrastructure.entity.Operator
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.llm.protocol.enums.ProviderProtocol

/**
 * LLM领域服务
 */
@Service
class LlmDomainService(
    private val providerRepository: ProviderRepository,
    private val modelRepository: ModelRepository
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
        providers
            .takeIf { it.isNotEmpty() }
            ?.let {
                val activeModelsByProviderId = modelRepository.selectList(
                    KtQueryWrapper(ModelEntity::class.java)
                        .`in`(ModelEntity::providerId, it.mapNotNull(ProviderEntity::id))
                        .eq(ModelEntity::status, true)
                ).groupBy(ModelEntity::providerId)

                it.map { provider -> ProviderAggregate(provider, activeModelsByProviderId[provider.id]) }
            }
            ?: emptyList()

    fun getProvider(providerId: String, userId: String): ProviderEntity =
        providerRepository.selectOne(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::id, providerId)
                .eq(ProviderEntity::userId, userId)
                .or()
                .eq(ProviderEntity::id, providerId)
                .eq(ProviderEntity::isOfficial, true)
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
        providerRepository.checkedDelete(
            KtQueryWrapper(ProviderEntity::class.java)
                .eq(ProviderEntity::id, providerId)
                .eq(operator.needCheckUserId(), ProviderEntity::userId, userId)
        )
        modelRepository.delete(
            KtQueryWrapper(ModelEntity::class.java)
                .eq(ModelEntity::providerId, providerId)
        )
    }

    private fun validateProviderProtocol(protocol: ProviderProtocol?) {
        if (protocol == null || protocol !in ProviderProtocol.entries) {
            throw BusinessException("不支持的服务商协议类型: $protocol")
        }
    }

    fun getProviderProtocols(): List<ProviderProtocol> = ProviderProtocol.entries

    fun createModel(model: ModelEntity) {
        modelRepository.insert(model)
    }

    fun updateModel(model: ModelEntity) {
        modelRepository.checkedUpdate(
            model,
            KtUpdateWrapper(ModelEntity::class.java)
                .eq(ModelEntity::id, model.id)
                .eq(ModelEntity::userId, model.userId)
        )
    }

    fun deleteModel(modelId: String, userId: String, operator: Operator) {
        modelRepository.checkedDelete(
            KtQueryWrapper(ModelEntity::class.java)
                .eq(ModelEntity::id, modelId)
                .eq(operator.needCheckUserId(), ModelEntity::userId, userId)
        )
    }

    fun updateModelStatus(modelId: String, userId: String) {
        val model = modelRepository.selectOne(
            KtQueryWrapper(ModelEntity::class.java)
                .eq(ModelEntity::id, modelId)
                .eq(ModelEntity::userId, userId)
        ) ?: throw BusinessException("模型不存在")

        model.status = !model.status
        modelRepository.checkedUpdateById(model)
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
}
