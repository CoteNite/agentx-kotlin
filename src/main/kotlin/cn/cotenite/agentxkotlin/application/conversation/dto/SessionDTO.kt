package cn.cotenite.agentxkotlin.application.conversation.dto

import java.time.LocalDateTime

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 17:17
 */
data class SessionDTO(
    val id:String,
    val agentId:String?,
    val title:String,
    val description: String?,
    val createdAt:LocalDateTime?,
    val updatedAt:LocalDateTime?,
    val isArchived:Boolean
){
    companion object{

    }
}
