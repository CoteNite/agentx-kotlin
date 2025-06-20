package cn.cotenite.agentxkotlin.domain.agent.constant

import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 11:40
 */
enum class PublishStatus(
    val  code:Int,
    val description:String
){

    /**
     * 审核中状态
     */
    REVIEWING(1, "审核中"),

    /**
     * 已发布状态
     */
    PUBLISHED(2, "已发布"),

    /**
     * 发布拒绝状态
     */
    REJECTED(3, "拒绝"),

    /**
     * 已下架状态
     */
    REMOVED(4, "已下架");


    companion object{
        fun fromCode(code: Int): PublishStatus {
            return entries.firstOrNull { it.code == code } ?: throw BusinessException("INVALID_STATUS_CODE", "无效的发布状态码: $code")
        }
    }
}
