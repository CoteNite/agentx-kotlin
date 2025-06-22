package cn.cotenite.agentxkotlin.domain.llm.model.enums

import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException



/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:06
 */
enum class ModelType(
    val code: String,
    val description: String
){

    CHAT("CHAT", "对话模型"),
    EMBEDDING("EMBEDDING", "嵌入模型");

    companion object{

        fun fromCode(code: String?): ModelType {
            for (type in entries) {
                if (type.code == code) {
                    return type
                }
            }
            throw BusinessException("Unknown model type code: $code")
        }
    }


}