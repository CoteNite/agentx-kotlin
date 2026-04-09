package cn.cotenite.domain.tool.constant

import cn.cotenite.infrastructure.exception.BusinessException

/**
 * @author  yhk
 * Description  
 * Date  2026/4/6 20:53
 */
enum class ToolType {

    MCP
    ;

    companion object{
        fun fromCode(code: String): ToolType{
            ToolType.entries.forEach { type ->
                if (type.name == code){
                    return type
                }
            }
            throw BusinessException("未知的工具类型码：$code")
        }
    }

}