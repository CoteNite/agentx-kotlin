package cn.cotenite.agentxkotlin.domain.token.service.impl

import cn.cotenite.agentxkotlin.domain.token.model.TokenMessage
import cn.cotenite.agentxkotlin.domain.token.model.TokenProcessResult
import cn.cotenite.agentxkotlin.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.agentxkotlin.domain.token.model.enums.TokenOverflowStrategyEnum
import cn.cotenite.agentxkotlin.domain.token.service.TokenOverflowStrategy
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 02:12
 */
@Service
class NoTokenOverflowStrategy(
    private val config: TokenOverflowConfig= TokenOverflowConfig.createDefault(),
): TokenOverflowStrategy{

    /**
     * 处理消息列表，无策略实现不做任何处理，返回原消息列表
     *
     * @param messages 待处理的消息列表
     * @return 原消息列表，不做修改
     */
    override fun process(messages: MutableList<TokenMessage>): TokenProcessResult {
        return TokenProcessResult(
            retainedMessages = messages,
            totalTokens = this.calculateTotalTokens(messages),
            strategyName = this.getName(),
            processed = false
        )
    }

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    override fun getName(): String {
        return TokenOverflowStrategyEnum.NONE.name
    }

    /**
     * 是否需要进行Token超限处理
     * 无策略实现始终返回false，表示不需要处理
     *
     * @param messages 待处理的消息列表
     * @return 始终返回false，表示不处理
     */
    override fun needsProcessing(messages: MutableList<TokenMessage>): Boolean {
        return false
    }

    /**
     * 计算消息列表的总token数
     */
    private fun calculateTotalTokens(messages: MutableList<TokenMessage>): Int {
        return messages.sumOf {  it.tokenCount }
    }

}