package cn.cotenite.domain.highavailability.service

import cn.cotenite.domain.highavailability.gateway.HighAvailabilityGateway
import cn.cotenite.domain.llm.event.ModelsBatchDeletedEvent
import cn.cotenite.domain.llm.model.HighAvailabilityResult
import cn.cotenite.domain.llm.model.ModelEntity
import cn.cotenite.domain.llm.model.ProviderEntity
import cn.cotenite.domain.llm.service.HighAvailabilityDomainService
import cn.cotenite.domain.llm.service.LlmDomainService
import cn.cotenite.infrastructure.config.HighAvailabilityProperties
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceBatchDeleteRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceUpdateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ProjectCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ReportResultRequest
import cn.cotenite.infrastructure.highavailability.dto.request.SelectInstanceRequest
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/**
 * 高可用领域服务实现
 * 负责高可用相关的业务逻辑和策略决策
 */
@Service
class HighAvailabilityDomainServiceImpl(
    private val properties: HighAvailabilityProperties,
    private val gateway: HighAvailabilityGateway,
    private val llmDomainService: LlmDomainService
) : HighAvailabilityDomainService {

    private val logger = LoggerFactory.getLogger(HighAvailabilityDomainServiceImpl::class.java)

    override fun syncModelToGateway(model: ModelEntity) {
        if (!properties.enabled) {
            logger.debug("高可用功能未启用，跳过模型同步: {}", model.id)
            return
        }

        try {
            val request = ApiInstanceCreateRequest(model.userId, model.modelId, "MODEL", model.id)
            gateway.createApiInstance(request)
            logger.info("成功同步模型到高可用网关: modelId={}", model.id)
        } catch (e: Exception) {
            logger.error("同步模型到高可用网关失败: modelId={}", model.id, e)
            throw BusinessException("同步模型到高可用网关失败", e)
        }
    }

    override fun removeModelFromGateway(modelId: String, userId: String) {
        if (!properties.enabled) {
            logger.debug("高可用功能未启用，跳过模型删除: {}", modelId)
            return
        }

        try {
            gateway.deleteApiInstance("MODEL", modelId)
            logger.info("成功从高可用网关删除模型: modelId={}", modelId)
        } catch (e: Exception) {
            logger.error("从高可用网关删除模型失败: modelId={}", modelId, e)
        }
    }

    override fun updateModelInGateway(model: ModelEntity) {
        if (!properties.enabled) {
            logger.debug("高可用功能未启用，跳过模型更新: {}", model.id)
            return
        }

        try {
            val request = ApiInstanceUpdateRequest(
                model.userId,
                model.modelId,
                null, // routingParams
                null // metadata
            )

            gateway.updateApiInstance("MODEL", model.id ?: return, request)
            logger.info("成功更新高可用网关中的模型: modelId={}", model.id)
        } catch (e: Exception) {
            logger.error("更新高可用网关中的模型失败: modelId={}", model.id, e)
        }
    }

    override fun selectBestProvider(model: ModelEntity, userId: String): HighAvailabilityResult {
        return selectBestProvider(model, userId, "")
    }

    override fun selectBestProvider(model: ModelEntity, userId: String, sessionId: String): HighAvailabilityResult {
        return selectBestProvider(model, userId, sessionId, null)
    }

    override fun selectBestProvider(
        model: ModelEntity,
        userId: String,
        sessionId: String,
        fallbackChain: List<String>?
    ): HighAvailabilityResult {
        if (!properties.enabled) {
            logger.debug("高可用功能未启用，使用默认Provider选择逻辑: modelId={}", model.id)
            val provider = llmDomainService.getProvider(
                model.providerId ?: throw BusinessException("模型Provider不存在"),
                userId
            )
            return HighAvailabilityResult().apply {
                this.provider = provider
                this.model = model
                this.instanceId = null
            }
        }

        try {
            val request = SelectInstanceRequest(userId, model.modelId, "MODEL")

            if (sessionId.isNotBlank()) {
                request.affinityKey = sessionId
                request.affinityType = "SESSION"
                logger.debug("启用会话亲和性: sessionId={}, modelId={}", sessionId, model.id)
            }

            if (!fallbackChain.isNullOrEmpty()) {
                request.fallbackChain = fallbackChain
                logger.debug(
                    "启用降级链: userId={}, primaryModel={}, fallbackModels={}",
                    userId,
                    model.modelId,
                    fallbackChain
                )
            }

            val selectedInstance = gateway.selectBestInstance(request)
            val businessId = selectedInstance.businessId ?: throw BusinessException("网关返回businessId为空")
            val instanceId = selectedInstance.id

            val bestModel = llmDomainService.getModelById(businessId)
            val provider = llmDomainService.getProvider(
                bestModel.providerId ?: throw BusinessException("模型Provider不存在"),
                userId
            )

            logger.info(
                "通过高可用网关选择Provider成功: modelId={}, bestBusinessId={}, providerId={}, sessionId={}",
                model.id,
                businessId,
                provider.id,
                sessionId
            )

            return HighAvailabilityResult().apply {
                this.provider = provider
                this.model = bestModel
                this.instanceId = instanceId
            }
        } catch (e: Exception) {
            logger.warn("高可用网关选择Provider失败，降级到默认逻辑: modelId={}, sessionId={}", model.id, sessionId, e)

            try {
                val provider = llmDomainService.getProvider(
                    model.providerId ?: throw BusinessException("模型Provider不存在"),
                    userId
                )
                return HighAvailabilityResult().apply {
                    this.provider = provider
                    this.model = model
                    this.instanceId = null
                }
            } catch (fallbackException: Exception) {
                logger.error("降级逻辑也失败了: modelId={}, sessionId={}", model.id, sessionId, fallbackException)
                throw BusinessException("获取Provider失败", fallbackException)
            }
        }
    }

    @Async
    override fun reportCallResult(
        instanceId: String?,
        modelId: String,
        success: Boolean,
        latencyMs: Long,
        errorMessage: String?
    ) {
        if (!properties.enabled) {
            return
        }

        try {
            val request = ReportResultRequest().apply {
                this.instanceId = instanceId
                this.businessId = modelId
                this.success = success
                this.latencyMs = latencyMs
                this.errorMessage = errorMessage
                this.callTimestamp = System.currentTimeMillis()
            }

            gateway.reportResult(request)

            logger.debug(
                "成功上报调用结果: instanceId={}, modelId={}, success={}, latency={}ms",
                instanceId,
                modelId,
                success,
                latencyMs
            )
        } catch (e: Exception) {
            logger.error("上报调用结果失败: instanceId={}, modelId={}", instanceId, modelId, e)
        }
    }

    override fun initializeProject() {
        if (!properties.enabled) {
            logger.info("高可用功能未启用，跳过项目初始化")
            return
        }

        try {
            val projectRequest = ProjectCreateRequest("AgentX", "AgentX高可用项目", properties.apiKey)
            gateway.createProject(projectRequest)
            logger.info("高可用项目初始化成功")
        } catch (e: Exception) {
            logger.error("高可用项目初始化失败", e)
        }
    }

    override fun syncAllModelsToGateway() {
        if (!properties.enabled) {
            logger.info("高可用功能未启用，跳过模型批量同步")
            return
        }

        try {
            val allActiveModels = llmDomainService.getAllActiveModels().filterNotNull()
            if (allActiveModels.isEmpty()) {
                logger.info("没有激活的模型需要同步")
                return
            }

            val instanceRequests = allActiveModels.map { model ->
                ApiInstanceCreateRequest(model.userId, model.modelId, "MODEL", model.id)
            }

            gateway.batchCreateApiInstances(instanceRequests)
            logger.info("成功批量同步{}个模型到高可用网关", allActiveModels.size)
        } catch (e: Exception) {
            logger.error("批量同步模型到高可用网关失败", e)
        }
    }

    override fun changeModelStatusInGateway(model: ModelEntity, enabled: Boolean, reason: String) {
        if (!properties.enabled) {
            logger.debug("高可用功能未启用，跳过模型状态变更: {}", model.id)
            return
        }

        try {
            if (enabled) {
                gateway.activateApiInstance("MODEL", model.id ?: return)
                logger.info("成功启用高可用网关中的模型: modelId={}, reason={}", model.id, reason)
            } else {
                gateway.deactivateApiInstance("MODEL", model.id ?: return)
                logger.info("成功禁用高可用网关中的模型: modelId={}, reason={}", model.id, reason)
            }
        } catch (e: Exception) {
            logger.error("变更高可用网关中的模型状态失败: modelId={}, enabled={}", model.id, enabled, e)
        }
    }

    override fun batchRemoveModelsFromGateway(deleteItems: List<ModelsBatchDeletedEvent.ModelDeleteItem>, userId: String) {
        if (!properties.enabled) {
            logger.debug("高可用功能未启用，跳过批量模型删除: 用户={}, 数量={}", userId, deleteItems.size)
            return
        }

        if (deleteItems.isEmpty()) {
            logger.debug("没有要删除的模型")
            return
        }

        try {
            val instances = deleteItems.map { deleteItem ->
                ApiInstanceBatchDeleteRequest.ApiInstanceDeleteItem("MODEL", deleteItem.modelId)
            }

            gateway.batchDeleteApiInstances(instances)
            logger.info("成功批量删除{}个模型从高可用网关，用户ID: {}", deleteItems.size, userId)
        } catch (e: Exception) {
            logger.error("批量删除模型从高可用网关失败，用户ID: {}, 数量: {}", userId, deleteItems.size, e)
            // 批量删除失败不抛异常，避免影响主流程
        }
    }
}
