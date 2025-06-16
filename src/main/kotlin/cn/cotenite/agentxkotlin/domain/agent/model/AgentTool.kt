package cn.cotenite.agentxkotlin.domain.agent.model

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 02:57
 */
data class AgentTool(
    val id:String,
    val name:String,
    val description:String,
    val type:String,
    val permissions:String,
    val config:Any,
){


}
