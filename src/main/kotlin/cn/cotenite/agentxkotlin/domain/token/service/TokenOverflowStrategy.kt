package cn.cotenite.agentxkotlin.domain.token.service

import cn.cotenite.agentxkotlin.domain.token.model.TokenMessage
import cn.cotenite.agentxkotlin.domain.token.model.TokenProcessResult
import org.springframework.stereotype.Service


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/21 00:14
 */
@Service
interface TokenOverflowStrategy {

    /**
     * 处理消息列表
     *
     * @param messages 待处理的消息列表
     * @return 处理结果，包含处理后的消息列表、摘要等信息
     */
    fun process(messages: MutableList<TokenMessage>): TokenProcessResult

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    fun getName(): String

    /**
     * 检查是否需要处理
     *
     * @param messages 待检查的消息列表
     * @return 是否需要处理
     */
    fun needsProcessing(messages: MutableList<TokenMessage>): Boolean

}