package cn.cotenite.domain.tool.service

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.repository.ToolRepository
import cn.cotenite.domain.tool.service.state.ToolStateProcessor
import cn.cotenite.domain.tool.service.state.impl.*
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.github.GitHubService
import cn.cotenite.infrastructure.mcpGateway.MCPGatewayService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 *工具状态流转服务。 管理工具在不同状态间的转换，并执行各状态对应的处理逻辑
 */
@Service
class ToolStateDomainService(
    private val toolRepository: ToolRepository,
    private val gitHubService: GitHubService,
    private val mcpGatewayService: MCPGatewayService
){

    private val logger: Logger = LoggerFactory.getLogger(ToolStateDomainService::class.java)
    private val processorMap = mutableMapOf<ToolStatus, ToolStateProcessor>()

    // 定义协程作用域：SupervisorJob 保证子任务失败不影响父级，Dispatchers.Default 处理逻辑
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


    @PostConstruct
    fun init() {
        // 注册处理器
        registerProcessor(WaitingReviewProcessor())
        registerProcessor(GithubUrlValidateProcessor(gitHubService))
        registerProcessor(DeployingProcessor(mcpGatewayService))
        registerProcessor(FetchingToolsProcessor(mcpGatewayService))
        registerProcessor(PublishingProcessor(gitHubService))
        registerProcessor(ManualReviewProcessor())

        logger.info("工具状态处理器初始化完成，已注册 ${processorMap.size} 个处理器。")
    }

    private fun registerProcessor(processor: ToolStateProcessor) {
        processorMap[processor.getStatus()]?.let {
            logger.warn("状态 ${processor.getStatus()} 的处理器已被覆盖。原: ${it::class.java.name}, 新: ${processor::class.java.name}")
        }
        processorMap[processor.getStatus()] = processor
    }


    /** 处理人工审核完成的工具。 由外部调用（如后台管理系统）来驱动人工审核后的状态流转。
     *
     * @param tool 要处理的工具。
     * @param approved 审核结果，true表示批准，false表示拒绝。
     **/
    @Transactional
    fun manualReviewComplete(tool: ToolEntity, approved: Boolean): String{
        val toolId = tool.id!!
        tool.status.takeIf { it!= ToolStatus.MANUAL_REVIEW }?.let {
            logger.warn("工具ID：$toolId 当前的状态不是MANUAL_REVIEW(${tool.status})，忽略人工审核完成操作")
            return toolId
        }

        approved.takeIf { it }?.let {
            tool.status=ToolStatus.APPROVED // 审核通过，进入发布状态
            toolRepository.updateById(tool) // 保存APPROVED状态
            logger.info("工具ID: {} 人工审核通过，状态更新为 APPROVED。", toolId)
            submitToolForProcessing(tool)
        }?:run {
            tool.status=ToolStatus.FAILED
            toolRepository.updateById(tool)
            logger.info("工具ID: {} 人工审核失败，状态更新为 FAILED。", toolId)
        }
        return toolId
    }

    fun submitToolForProcessing(toolEntity: ToolEntity) {
        logger.info("提交工具ID: {} (当前状态: {}) 到状态处理队列。", toolEntity.id, toolEntity.status)
        // 启动一个后台协程执行处理逻辑
        serviceScope.launch {
            processToolState(toolEntity)
        }
    }

    /**
     * 核心状态处理逻辑（递归或循环）
     */
    private suspend fun processToolState(toolEntity: ToolEntity) {
        val initialStatus = toolEntity.status?:throw BusinessException("出现内部错误")
        val processor = processorMap[initialStatus] ?: run {
            logger.warn("工具ID: ${toolEntity.id} 状态 $initialStatus 未找到处理器。")
            return
        }

        try {
            processor.process(toolEntity)

            val nextStatusCandidate = processor.getNextStatus()
            if (nextStatusCandidate != initialStatus) {
                toolEntity.status = nextStatusCandidate
                toolRepository.updateById(toolEntity)
                logger.info("工具ID: {} 状态从 {} 更新为 {}。", toolEntity.id, initialStatus, nextStatusCandidate);
                if (nextStatusCandidate == ToolStatus.MANUAL_REVIEW) {
                    logger.info("工具ID: {} 进入MANUAL_REVIEW状态，等待人工审核。", toolEntity.id)
                    return
                }

                // 递归调用：协程递归性能极高且不会造成线程阻塞
                processToolState(toolEntity)
            }
        } catch (e: Exception) {
            handleFailure(toolEntity, initialStatus, e)
        }
    }

    private suspend fun handleFailure(tool: ToolEntity, status: ToolStatus, e: Exception) {
        logger.error("处理工具ID: ${tool.id} 失败: ${e.message}")
        tool.status= ToolStatus.FAILED
        tool.failedStepStatus = status
        tool.rejectReason = "状态处理失败: ${e.message}"
        withContext(Dispatchers.IO) {
            toolRepository.updateById(tool)
        }
    }

}
