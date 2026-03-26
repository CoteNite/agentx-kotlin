package cn.cotenite.domain.token.service

import org.springframework.stereotype.Service
import cn.cotenite.domain.token.model.TokenMessage
import cn.cotenite.domain.token.model.TokenProcessResult
import cn.cotenite.domain.token.model.config.TokenOverflowConfig

/**
 * Token领域服务
 */
@Service
class TokenDomainService(
    private val strategyFactory: TokenOverflowStrategyFactory
) {

    fun processMessages(messages: List<TokenMessage>, config: TokenOverflowConfig): TokenProcessResult =
        strategyFactory.createStrategy(config).process(messages, config)
}
