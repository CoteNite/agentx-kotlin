package cn.cotenite.agentxkotlin.domain.llm.model.enums

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:09
 */
enum class ProviderType(
    val code: String,
){

    /**
     * 所有服务商(包括官方和用户自定义)
     */
    ALL("all"),

    /**
     * 官方服务商
     */
    OFFICIAL("official"),

    /**
     * 用户自定义服务商
     */
    CUSTOM("custom");

    companion object{
        /**
         * 根据code获取对应的枚举值
         * @param code 类型编码
         * @return 对应的枚举，若不存在则默认返回USER类型
         */
        fun fromCode(code: String?): ProviderType? {
            for (type in entries) {
                if (type.code == code) {
                    return type
                }
            }
            return ALL
        }
    }


}