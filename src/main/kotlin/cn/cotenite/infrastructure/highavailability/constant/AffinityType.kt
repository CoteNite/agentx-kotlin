package cn.cotenite.infrastructure.highavailability.constant

/**
 * @author  yhk
 * Description  
 * Date  2026/4/18 20:51
 */
object AffinityType {

    /** 会话亲和性 - 同一会话的请求路由到同一实例  */
    val SESSION: String = "SESSION"

    /** 用户亲和性 - 同一用户的请求路由到同一实例  */
    val USER: String = "USER"

    /** 批次亲和性 - 同一批次的请求路由到同一实例  */
    val BATCH: String = "BATCH"

    /** 地域亲和性 - 同一地域的请求路由到同一实例  */
    val REGION: String = "REGION"
    
}