package cn.cotenite.agentxkotlin.domain.llm.model

import cn.cotenite.agentxkotlin.domain.llm.model.config.ProviderConfig
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:18
 */
class ProviderAggregate(
    val entity: ProviderEntity, // 聚合根持有对实体的引用
    models: List<ModelEntity>? = null // 构造函数参数，可空并提供默认值
) {
    // 使用 var 定义 models，因为它是可变的列表
    var models: MutableList<ModelEntity> = models?.toMutableList() ?: ArrayList()

    init {
        // 可以在 init 块中执行额外的初始化逻辑，例如验证
        // ensure models belong to this provider if any provided in constructor
        this.models.removeIf { it.providerId != entity.id } // 移除不属于当前提供商的模型
    }

    /**
     * 添加模型
     */
    fun addModel(model: ModelEntity) {
        // 领域规则：只有属于当前提供商的模型才能被添加
        if (model.providerId == entity.id && !this.models.contains(model)) {
            this.models.add(model)
        }
    }

    /**
     * 设置模型列表
     * 注意：这里假设传入的模型列表已经过滤过，或者在 addModel 中再次过滤。
     * 为确保一致性，建议在设置时也进行过滤。
     */
    fun setModels(models: List<ModelEntity>) {
        this.models = models.filter { it.providerId == entity.id }.toMutableList()
    }

    /**
     * 获取模型列表
     * @return 返回不可修改的列表视图，防止外部意外修改内部列表
     */
    fun getModels(): List<ModelEntity> {
        return models.toList() // 返回一个只读的副本
    }

    /**
     * 获取服务商配置（解密版本）
     * 直接委托给 entity
     */
    fun getConfig(): ProviderConfig? = entity.config

    /**
     * 设置服务商配置（会自动加密）
     * 直接委托给 entity
     */
    fun setConfig(config: ProviderConfig?) {
        entity.config = config
    }

    // --- 委托属性 (Delegated Properties) ---
    // 对于很多直接代理给 entity 的 getter/setter，可以使用 Kotlin 的委托属性来简化。
    // 但是对于简单的 getter/setter，直接手动代理也行。这里为了演示，我会混合使用。

    val id: String?
        get() = entity.id

    val userId: String?
        get() = entity.userId

    var protocol: ProviderProtocol?
        get() = entity.protocol
        set(value) { entity.protocol = value }

    var name: String?
        get() = entity.name
        set(value) { entity.name = value }

    var description: String?
        get() = entity.description
        set(value) { entity.description = value }

    var isOfficial: Boolean
        get() = entity.isOfficial
        set(value) { entity.isOfficial = value }

    var status: Boolean
        get() = entity.status
        set(value) { entity.status = value }

    val createdAt: LocalDateTime?
        get() = entity.createdAt

    val updatedAt: LocalDateTime?
        get() = entity.updatedAt

    val deletedAt: LocalDateTime?
        get() = entity.deletedAt

    /**
     * 获取原始实体
     */
    fun getEntity(): ProviderEntity {
        return entity
    }

    /**
     * 检查服务商是否激活 (委托给实体，但这里聚合根可以加入聚合层面的校验)
     */
    fun isActive() {
        entity.isActive()
    }
}