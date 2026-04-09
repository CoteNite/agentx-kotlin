package cn.cotenite.domain.tool.constant

import cn.cotenite.infrastructure.exception.BusinessException

/**
 * @author  yhk
 * Description  
 * Date  2026/4/6 20:58
 */
enum class UploadType {

    GITHUB,
    ZIP
    ;

    companion object{
        fun fromCode(code: String): UploadType{
            UploadType.entries.forEach { type ->
                if (type.name==code){
                    return type
                }
            }
            throw BusinessException("未知的上传类型码: $code")
        }
    }
}