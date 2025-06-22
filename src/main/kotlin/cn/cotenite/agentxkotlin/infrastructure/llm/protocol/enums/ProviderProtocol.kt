package cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums

import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException


/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:19
 */
enum class ProviderProtocol {

    OpenAI;


    companion object{
        fun fromCode(code: String?): ProviderProtocol {
            for (protocol in entries) {
                if (protocol.name == code) {
                    return protocol
                }
            }
            throw BusinessException("Unknown model type code: $code")
        }
    }


}