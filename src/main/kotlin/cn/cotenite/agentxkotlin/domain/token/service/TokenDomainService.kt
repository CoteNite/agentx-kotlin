package cn.cotenite.agentxkotlin.domain.token.service

import cn.cotenite.agentxkotlin.domain.token.model.TokenMessage
import cn.cotenite.agentxkotlin.domain.token.model.TokenProcessResult
import cn.cotenite.agentxkotlin.domain.token.model.config.TokenOverflowConfig
import cn.cotenite.agentxkotlin.domain.token.service.factory.TokenOverflowStrategyFactory
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 00:08
 */
@Service
class TokenDomainService(
    private val strategyFactory: TokenOverflowStrategyFactory
){

    /**
     * 处理消息列表
     *
     * @param messages 待处理的消息列表
     * @param config 处理配置
     * @return 处理结果
     */
    fun processMessages(messages: MutableList<TokenMessage>, config: TokenOverflowConfig): TokenProcessResult {
        val strategy = strategyFactory.createStrategy(config)
        return strategy.process(messages)
    }

    /**
     * 计算消息列表的总Token数
     *
     * @param messages 消息列表
     * @return 总Token数
     */
    private fun calculateTotalTokens(messages: MutableList<TokenMessage>): Int {
        return messages.sumOf { it.tokenCount }
    }



}


