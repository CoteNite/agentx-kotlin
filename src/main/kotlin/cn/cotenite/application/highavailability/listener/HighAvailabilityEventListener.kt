package cn.cotenite.application.highavailability.listener

import cn.cotenite.domain.llm.event.ModelCreatedEvent
import cn.cotenite.domain.llm.event.ModelDeletedEvent
import cn.cotenite.domain.llm.event.ModelStatusChangedEvent
import cn.cotenite.domain.llm.event.ModelUpdatedEvent
import cn.cotenite.domain.llm.event.ModelsBatchDeletedEvent
import cn.cotenite.domain.llm.service.HighAvailabilityDomainService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

/** 高可用事件处理器 负责处理模型相关的领域事件，同步到高可用网关
 * @author  yhk
 * Date  2026/4/18 22:42
 */
@Component
class HighAvailabilityEventListener(
    private val highAvailabilityDomainService: HighAvailabilityDomainService
){

    private val logger: Logger = LoggerFactory.getLogger(HighAvailabilityEventListener::class.java)


    /** 处理模型创建事件 将新创建的模型同步到高可用网关  */
    @EventListener
    @Async
    fun handleModelCreated(event: ModelCreatedEvent) {
        try {
            logger.info(
                "处理模型创建事件: modelId={}, userId={}",
                event.modelId,
                event.userId
            )

            highAvailabilityDomainService.syncModelToGateway(event.model)

            logger.info("模型创建事件处理成功: modelId={}", event.modelId)
        } catch (e: Exception) {
            logger.error("处理模型创建事件失败: modelId={}", event.modelId, e)
        }
    }

    /** 处理模型更新事件 将更新的模型信息同步到高可用网关  */
    @EventListener
    @Async
    fun handleModelUpdated(event: ModelUpdatedEvent) {
        try {
            logger.info(
                "处理模型更新事件: modelId={}, userId={}",
                event.modelId,
                event.userId
            )

            highAvailabilityDomainService.updateModelInGateway(event.model)

            logger.info("模型更新事件处理成功: modelId={}", event.modelId)
        } catch (e: Exception) {
            logger.error("处理模型更新事件失败: modelId={}", event.modelId, e)
        }
    }

    /** 处理模型删除事件 从高可用网关中删除模型  */
    @EventListener
    @Async
    fun handleModelDeleted(event: ModelDeletedEvent) {
        try {
            logger.info(
                "处理模型删除事件: modelId={}, userId={}",
                event.modelId,
                event.userId
            )

            highAvailabilityDomainService.removeModelFromGateway(event.modelId, event.userId)

            logger.info("模型删除事件处理成功: modelId={}", event.modelId)
        } catch (e: Exception) {
            logger.error("处理模型删除事件失败: modelId={}", event.modelId, e)
        }
    }

    /** 处理模型状态变更事件 将模型状态变更同步到高可用网关（启用/禁用）  */
    @EventListener
    @Async
    fun handleModelStatusChanged(event: ModelStatusChangedEvent) {
        try {
            logger.info(
                "处理模型状态变更事件: modelId={}, userId={}, enabled={}, reason={}", event.modelId,
                event.userId, event.enabled, event.reason
            )

            highAvailabilityDomainService.changeModelStatusInGateway(
                event.model, event.enabled,
                event.reason!!
            )

            logger.info(
                "模型状态变更事件处理成功: modelId={}, enabled={}",
                event.modelId,
                event.enabled
            )
        } catch (e: Exception) {
            logger.error(
                "处理模型状态变更事件失败: modelId={}, enabled={}",
                event.modelId,
                event.enabled,
                e
            )
        }
    }

    /** 处理模型批量删除事件 批量从高可用网关中删除模型  */
    @EventListener
    @Async
    fun handleModelsBatchDeleted(event: ModelsBatchDeletedEvent) {
        try {
            logger.info(
                "处理模型批量删除事件: 用户={}, 删除数量={}",
                event.userId,
                event.deleteItems.size
            )

            highAvailabilityDomainService.batchRemoveModelsFromGateway(event.deleteItems, event.userId)

            logger.info(
                "模型批量删除事件处理成功: 用户={}, 删除数量={}",
                event.userId,
                event.deleteItems.size
            )
        } catch (e: Exception) {
            logger.error(
                "处理模型批量删除事件失败: 用户={}, 删除数量={}",
                event.userId,
                event.deleteItems.size,
                e
            )
        }
    }
    
}