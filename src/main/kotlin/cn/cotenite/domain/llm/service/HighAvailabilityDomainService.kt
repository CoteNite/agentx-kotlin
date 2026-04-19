package cn.cotenite.domain.llm.service

import cn.cotenite.domain.llm.event.ModelsBatchDeletedEvent
import cn.cotenite.domain.llm.model.HighAvailabilityResult
import cn.cotenite.domain.llm.model.ModelEntity

/**
 * 高可用领域服务接口，定义高可用网关相关的领域操作
 *
 * @author xhy
 * @since 1.0.0
 */
interface HighAvailabilityDomainService {

    /**
     * 同步模型到高可用网关
     *
     * @param model 模型实体
     */
    fun syncModelToGateway(model: ModelEntity)

    /**
     * 从高可用网关删除模型
     *
     * @param modelId 模型 ID
     * @param userId 用户 ID
     */
    fun removeModelFromGateway(modelId: String, userId: String)

    /**
     * 更新高可用网关中的模型
     *
     * @param model 模型实体
     */
    fun updateModelInGateway(model: ModelEntity)

    /**
     * 通过高可用网关选择最佳 Provider 和 Model，如果高可用未启用或选择失败，则降级到默认逻辑
     *
     * @param model 模型实体
     * @param userId 用户 ID
     * @return 高可用选择结果（包含 Provider 和 Model）
     */
    fun selectBestProvider(model: ModelEntity, userId: String): HighAvailabilityResult

    /**
     * 通过高可用网关选择最佳 Provider 和 Model（支持会话亲和性），如果高可用未启用或选择失败，则降级到默认逻辑
     *
     * @param model 模型实体
     * @param userId 用户 ID
     * @param sessionId 会话 ID，用于会话亲和性
     * @return 高可用选择结果（包含 Provider 和 Model）
     */
    fun selectBestProvider(model: ModelEntity, userId: String, sessionId: String): HighAvailabilityResult

    /**
     * 通过高可用网关选择最佳 Provider 和 Model（支持会话亲和性和降级链），如果高可用未启用或选择失败，则降级到默认逻辑
     *
     * @param model 模型实体
     * @param userId 用户 ID
     * @param sessionId 会话 ID，用于会话亲和性
     * @param fallbackChain 降级模型链，为 null 时不启用降级
     * @return 高可用选择结果（包含 Provider 和 Model）
     */
    fun selectBestProvider(
        model: ModelEntity,
        userId: String,
        sessionId: String,
        fallbackChain: List<String>?
    ): HighAvailabilityResult

    /**
     * 上报调用结果到高可用网关
     *
     * @param instanceId 实例 ID（从 selectBestProvider 返回）
     * @param modelId 模型 ID
     * @param success 是否成功
     * @param latencyMs 延迟时间（毫秒）
     * @param errorMessage 错误信息（可选）
     */
    fun reportCallResult(
        instanceId: String?,
        modelId: String,
        success: Boolean,
        latencyMs: Long,
        errorMessage: String?
    )

    /** 初始化项目到高可用网关 */
    fun initializeProject()

    /** 批量同步所有模型到高可用网关 */
    fun syncAllModelsToGateway()

    /**
     * 变更模型在高可用网关中的状态
     *
     * @param model 模型实体
     * @param enabled true=启用，false=禁用
     * @param reason 状态变更原因
     */
    fun changeModelStatusInGateway(model: ModelEntity, enabled: Boolean, reason: String)

    /**
     * 批量从高可用网关删除模型
     *
     * @param deleteItems 要删除的模型列表
     * @param userId 用户 ID
     */
    fun batchRemoveModelsFromGateway(deleteItems: List<ModelsBatchDeletedEvent.ModelDeleteItem>, userId: String)
}
