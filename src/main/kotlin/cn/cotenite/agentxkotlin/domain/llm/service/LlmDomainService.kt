package cn.cotenite.agentxkotlin.domain.llm.service

import cn.cotenite.agentxkotlin.domain.llm.model.ModelEntity
import cn.cotenite.agentxkotlin.domain.llm.model.ProviderAggregate
import cn.cotenite.agentxkotlin.domain.llm.model.ProviderEntity
import cn.cotenite.agentxkotlin.domain.llm.model.enums.ProviderType
import cn.cotenite.agentxkotlin.domain.llm.repository.ModelRepository
import cn.cotenite.agentxkotlin.domain.llm.repository.ProviderRepository
import cn.cotenite.agentxkotlin.infrastructure.entity.Operator
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import jakarta.transaction.Transactional
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:20
 */
@Service
class LlmDomainService(
    private val providerRepository: ProviderRepository,
    private val modelRepository: ModelRepository,
){
    /**
     * 创建服务商
     * @param provider 服务商信息
     * @return 创建后的服务商实体
     */
    @Transactional
    fun createProvider(provider: ProviderEntity): ProviderEntity {
        validateProviderProtocol(provider.protocol) // 验证协议
        return providerRepository.save(provider) // JPA 的 save 方法会处理插入和更新
    }

    /**
     * 更新服务商
     * @param provider 服务商信息
     */
    @Transactional
    fun updateProvider(provider: ProviderEntity) {
        validateProviderProtocol(provider.protocol) // 验证协议

        // 检查服务商是否存在
        val existingProvider = provider.id?.let {
            providerRepository.findById(it)
                .orElseThrow { BusinessException("服务商不存在: ${provider.id}") }
        } ?: throw BusinessException("服务商ID不能为空")

        // 验证 userId 权限 (如果需要)
        // 假设 provider.needCheckUserId() 是 Operator 的一个方法或类似概念，用于判断是否需要校验用户ID
        // 如果 ProviderEntity 内部没有 needCheckUserId()，则此逻辑需要外部 Operator 提供
        // 这里简化处理：如果当前用户是该服务商的创建者，或者服务商是官方的，则允许更新
        // 实际场景中，这里的权限校验会更复杂，可能由 Operator 统一管理
        if (operatorNeedsCheckUserId(existingProvider.userId) && existingProvider.userId != provider.userId) {
            throw BusinessException("无权修改其他用户的服务商")
        }

        // JPA 的 save 会根据ID存在性自动判断是更新还是插入
        // 这里直接保存传入的 provider，JPA 会根据 ID 更新现有记录
        // 确保 provider 对象的其他字段已经被正确设置
        providerRepository.save(provider)
    }

    // 假设这是一个辅助函数，用于模拟 Java ProviderEntity 的 needCheckUserId() 逻辑
    private fun operatorNeedsCheckUserId(currentUserId: String?): Boolean {
        // TODO: 根据你的 Operator 逻辑实现，这里只是一个示例
        // 例如：return Operator.current().needCheckUserId();
        return currentUserId != null // 如果有 userId，就意味着需要检查
    }

    /**
     * 获取用户自己的服务商
     * @param userId 用户id
     */
    fun getUserProviders(userId: String): List<ProviderAggregate> {
        val providers = providerRepository.findByUserId(userId) // 使用 JpaRepository 自定义方法
        return buildProviderAggregatesWithActiveModels(providers)
    }

    /**
     * 获取所有服务商（包含官方和用户自定义）
     * @param userId 用户ID
     * @return 服务商聚合根列表
     */
    fun getAllProviders(userId: String): List<ProviderAggregate> {
        // JPA 原生查询或 Specification 实现 OR 逻辑
        val providers = providerRepository.findAll { root, query, cb ->
            cb.or(
                cb.equal(root.get<String>("userId"), userId),
                cb.isTrue(root.get("isOfficial"))
            )
        }
        return buildProviderAggregatesWithActiveModels(providers)
    }

    /**
     * 获取官方服务商
     * @return 官方服务商聚合根列表
     */
    fun getOfficialProviders(): List<ProviderAggregate> {
        val providers = providerRepository.findByIsOfficial(true) // 假设有 findByIsOfficial 方法
        return buildProviderAggregatesWithActiveModels(providers)
    }

    /**
     * 获取用户自定义服务商
     * @param userId 用户ID
     * @return 用户自定义服务商聚合根列表
     */
    fun getCustomProviders(userId: String): List<ProviderAggregate> {
        val providers = providerRepository.findByUserIdAndIsOfficial(userId, false) // 假设有此方法
        return buildProviderAggregatesWithActiveModels(providers)
    }

    /**
     * 构建服务商聚合根，只包含激活的模型
     * 注意：这里假设 ProviderEntity 内部的 models 集合是 LAZY 加载的，
     * 并且通过 ProviderAggregate 访问时会触发 JPA 加载。
     * 如果你需要 EAGER 加载或优化 N+1 问题，可以考虑在 Repository 层使用 @EntityGraph 或 JOIN FETCH。
     * 当前实现仍通过单独查询模型来构建，以匹配原 Java 逻辑。
     * @param providers 服务商实体列表
     * @return 服务商聚合根列表
     */
    private fun buildProviderAggregatesWithActiveModels(providers: List<ProviderEntity>): List<ProviderAggregate> {
        if (providers.isEmpty()) {
            return emptyList()
        }

        val providerIds = providers.mapNotNull { it.id } // 收集所有服务商ID，并过滤null
        if (providerIds.isEmpty()) {
            return emptyList()
        }

        // 查询所有激活的模型，按 providerId 分组
        val activeModels = modelRepository.findAll { root, query, cb ->
            cb.and(
                root.get<String>("provider").get<String>("id").`in`(providerIds), // 查找关联的 providerId
                cb.isTrue(root.get("status"))
            )
        }

        val modelMap: Map<String, List<ModelEntity>> = activeModels.groupBy { it.providerId ?: "unknown" }.filterKeys { it != "unknown" }

        return providers.map { provider ->
            val modelsForProvider = modelMap[provider.id]?.toList() ?: emptyList()
            ProviderAggregate(provider, modelsForProvider)
        }
    }

    /**
     * 获取服务商
     * @param providerId 服务商id
     * @param userId 用户id
     */
    fun getProvider(providerId: String, userId: String): ProviderEntity {
        // 使用 Repository 提供的 findById 和自定义查询
        return providerRepository.findById(providerId)
            .filter { it.userId == userId } // 过滤确保是该用户的服务商
            .orElseThrow { BusinessException("服务商不存在或无权访问") }
    }

    /**
     * 查找服务商
     * @param providerId 服务商id
     */
    fun findProviderById(providerId: String): ProviderEntity? {
        return providerRepository.findById(providerId).orElse(null)
    }

    /**
     * 检查服务商是否存在
     * @param providerId 服务商id
     * @param userId 用户id
     */
    fun checkProviderExists(providerId: String, userId: String) {
        val exists = providerRepository.existsById(providerId) &&
                providerRepository.findById(providerId).map { it.userId == userId }.orElse(false)
        if (!exists) {
            throw BusinessException("服务商不存在或无权访问")
        }
    }

    /**
     * 获取服务商聚合根
     * @param providerId 服务商ID
     * @param userId 用户ID
     */
    fun getProviderAggregate(providerId: String, userId: String): ProviderAggregate {
        val provider = getProvider(providerId, userId) // 获取服务商实体，会抛出 BusinessException
        return ProviderAggregate(provider)
    }

    /**
     * 获取模型列表
     * @param providerId 服务商ID
     * @param userId 用户ID
     */
    fun getModelList(providerId: String, userId: String): List<ModelEntity> {
        // JPA Repository 会处理关联查询
        return modelRepository.findByProviderIdAndUserId(providerId, userId) // 假设有此方法
    }

    /**
     * 获取激活的模型列表
     * @param providerId 服务商ID
     * @param userId 用户ID
     * @return 激活的模型列表
     */
    fun getActiveModelList(providerId: String, userId: String): List<ModelEntity> {
        return modelRepository.findByProviderIdAndUserIdAndStatus(providerId, userId, true) // 假设有此方法
    }

    /**
     * 删除服务商
     * @param providerId 服务商id
     * @param userId 用户id
     */
    @Transactional
    fun deleteProvider(providerId: String, userId: String, operator: Operator) {
        val provider = providerRepository.findById(providerId)
            .orElseThrow { BusinessException("服务商不存在") }

        if (operator.needCheckUserId() && provider.userId != userId) {
            throw BusinessException("无权删除其他用户的服务商")
        }
        val modelsToDelete = modelRepository.findAll({ root, query, cb ->
            cb.equal(root.get<ProviderEntity>("provider").get<String>("id"), providerId)
        })

        if (modelsToDelete.isNotEmpty()) {
            modelRepository.deleteAll(modelsToDelete) // 批量删除找到的模型
        }

        providerRepository.delete(provider)
    }

    /**
     * 验证服务商协议是否支持
     * @param protocol 协议
     */
    private fun validateProviderProtocol(protocol: ProviderProtocol?) {
        if (protocol == null || !isSupportedProvider(protocol)) {
            throw BusinessException("不支持的服务商协议类型: $protocol")
        }
    }

    /**
     * 检查是否是支持的服务商协议
     * @param protocol 服务商提供商编码
     */
    private fun isSupportedProvider(protocol: ProviderProtocol): Boolean {
        return ProviderProtocol.entries.any { it == protocol } // Kotlin 枚举的 values() 变为 entries
    }

    /**
     * 获取所有支持的服务商协议
     */
    fun getProviderProtocols(): List<ProviderProtocol> {
        return ProviderProtocol.entries.toList()
    }

    /**
     * 创建模型
     * @param model 模型信息
     */
    @Transactional
    fun createModel(model: ModelEntity) {
        modelRepository.save(model) // JPA 的 save 方法会处理插入和更新
    }

    /**
     * 修改模型
     * @param model 模型信息
     */
    @Transactional
    fun updateModel(model: ModelEntity) {
        val existingModel = model.id?.let {
            modelRepository.findById(it)
                .orElseThrow { BusinessException("模型不存在: ${model.id}") }
        } ?: throw BusinessException("模型ID不能为空")

        // 验证 userId 权限
        if (existingModel.userId != model.userId) {
            throw BusinessException("无权修改其他用户的模型")
        }

        modelRepository.save(model)
    }

    /**
     * 删除模型
     * @param modelId 模型id
     * @param userId 用户id
     */
    @Transactional
    fun deleteModel(modelId: String, userId: String, operator: Operator) {
        val model = modelRepository.findById(modelId)
            .orElseThrow { BusinessException("模型不存在") }

        if (operator.needCheckUserId() && model.userId != userId) {
            throw BusinessException("无权删除其他用户的模型")
        }

        modelRepository.delete(model)
    }

    /**
     * 修改模型状态
     * @param modelId 模型id
     * @param userId 用户id
     */
    @Transactional
    fun updateModelStatus(modelId: String, userId: String) {
        val model = modelRepository.findById(modelId)
            .orElseThrow { BusinessException("模型不存在") }

        if (model.userId != userId) {
            throw BusinessException("无权修改其他用户的模型状态")
        }

        model.status = !model.status // 切换状态
        modelRepository.save(model) // 保存更新后的实体
    }

    /**
     * 根据类型获取服务商
     * @param providerType 服务商类型编码
     * @param userId 用户ID
     * @return 服务商聚合根列表
     */
    fun getProvidersByType(providerType: ProviderType, userId: String): List<ProviderAggregate> {
        val providers = when (providerType) {
            ProviderType.OFFICIAL -> providerRepository.findByIsOfficial(true)
            ProviderType.CUSTOM -> providerRepository.findByUserIdAndIsOfficial(userId, false)
            ProviderType.ALL -> providerRepository.findAll { root, query, cb ->
                cb.or(
                    cb.equal(root.get<String>("userId"), userId),
                    cb.isTrue(root.get("isOfficial"))
                )
            }
        }
        return buildProviderAggregatesWithActiveModels(providers)
    }

    /**
     * 修改服务商状态
     * @param providerId 服务商id
     * @param userId 用户id
     */
    @Transactional
    fun updateProviderStatus(providerId: String, userId: String) {
        val provider = providerRepository.findById(providerId)
            .orElseThrow { BusinessException("服务商不存在") }

        if (provider.userId != userId) {
            throw BusinessException("无权修改其他用户的服务商状态")
        }

        provider.status = !provider.status // 切换状态
        providerRepository.save(provider) // 保存更新后的实体
    }

    /**
     * 获取模型
     * @param modelId 模型id
     */
    fun getModelById(modelId: String): ModelEntity {
        return modelRepository.findById(modelId)
            .orElseThrow { BusinessException("模型不存在") }
    }


}