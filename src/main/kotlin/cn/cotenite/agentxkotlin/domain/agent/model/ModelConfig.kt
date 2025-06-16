package cn.cotenite.agentxkotlin.domain.agent.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 02:57
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ModelConfig(
    val modelName:String="gpt-3.5-turbo",
    val temperature:Double=0.7,
    val topP:Double=1.0,
    val maxTokens:Int=2000,
    val loadMemory:Boolean=true,
    val systemMessage:String="",
){
}
