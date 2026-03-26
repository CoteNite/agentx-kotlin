package cn.cotenite.infrastructure.entity

/**
 * 操作人类型
 */
enum class Operator {
    USER,
    ADMIN;

    /**
     * 是否需要校验用户ID
     */
    fun needCheckUserId(): Boolean = this == USER
}
