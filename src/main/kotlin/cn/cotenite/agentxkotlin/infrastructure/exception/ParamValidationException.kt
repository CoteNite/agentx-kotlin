package cn.cotenite.agentxkotlin.infrastructure.exception

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/16 11:18
 */
class ParamValidationException:BusinessException{

    companion object{
        private const val DEFAULT_CODE: String = "PARAM_VALIDATION_ERROR"
    }

    constructor(message: String):super(DEFAULT_CODE,message)

    constructor(paramName: String, message: String):super(DEFAULT_CODE, "参数${paramName}无效:${message}")


}
