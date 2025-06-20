package cn.cotenite.agentxkotlin.domain.agent.constant

import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 11:11
 */
enum class AgentStatus(
    private val code:Int,
    private val description:String
){

    /**
     * 草稿状态
     */
    DRAFT(0, "草稿"),

    /**
     * 待审核状态
     */
    PENDING_REVIEW(1, "待审核"),

    /**
     * 已上架状态
     */
    PUBLISHED(2, "已上架"),

    /**
     * 已下架状态
     */
    UNPUBLISHED(3, "已下架"),

    /**
     * 审核拒绝状态
     */
    REJECTED(4, "审核拒绝");

    companion object{
        fun fromCode(code: Int): AgentStatus{
            return entries.firstOrNull { it.code == code } ?: throw BusinessException("INVALID_AGENT_STATUS", "无效的Agent状态码: $code")
        }
    }


}
