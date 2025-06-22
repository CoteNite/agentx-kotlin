package cn.cotenite.agentxkotlin.domain.conversation.constant

import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 00:48
 */
enum class Role {
    USER,
    SYSTEM,
    ASSISTANT;

    companion object {
        fun fromCode(code: String): Role {
            for (role in entries) {
                if (role.name == code) {
                    return role
                }
            }
            throw BusinessException("Unknown model type code: " + code)
        }
    }
}
