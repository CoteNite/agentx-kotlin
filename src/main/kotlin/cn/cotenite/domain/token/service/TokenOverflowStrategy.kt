package cn.cotenite.domain.token.service

import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.TokenProcessResult
import cn.cotenite.domain.token.model.config.TokenOverflowConfig

/**
 * Token溢出策略
 */
interface TokenOverflowStrategy {
    fun process(messages: List<TokenMessage>, tokenOverflowConfig: TokenOverflowConfig): TokenProcessResult

    fun getName(): String

    fun needsProcessing(messages: List<TokenMessage>): Boolean
}
