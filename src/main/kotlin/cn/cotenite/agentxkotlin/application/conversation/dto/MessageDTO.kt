package cn.cotenite.agentxkotlin.application.conversation.dto

import cn.cotenite.agentxkotlin.domain.conversation.constant.Role
import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:17
 */
data class MessageDTO(
    val id:String?,
    val role:Role?,
    val content:String?,
    val createdAt: LocalDateTime?,
    val provider:String?=null,
    val model:String?=null
)
