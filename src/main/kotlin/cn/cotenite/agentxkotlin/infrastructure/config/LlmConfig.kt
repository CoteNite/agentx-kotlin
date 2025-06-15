package cn.cotenite.agentxkotlin.infrastructure.config

import cn.cotenite.agentxkotlin.domain.llm.service.LlmService
import cn.cotenite.agentxkotlin.infrastructure.integration.llm.siliconflow.SiliconFlowLlmService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/15 20:37
 */
@Configuration
class LlmConfig(
    @Value("\${llm.provider.default:siliconflow}")
    private val defaultProvider: String,
){

    @Bean
    @Primary
    fun defaultLlmService(siliconFlowLlmService: SiliconFlowLlmService): LlmService {
        return siliconFlowLlmService;
    }

    @Bean
    fun llmServiceMap(siliconFlowLlmService: SiliconFlowLlmService): Map<String, LlmService> {
        val serviceMap: MutableMap<String, LlmService> = HashMap()
        serviceMap["siliconflowLlmService"] = siliconFlowLlmService
        return serviceMap
    }
}
