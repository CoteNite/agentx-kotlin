package cn.cotenite.agentxkotlin.infrastructure.entity

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:12
 */
enum class Operator {

    USER,
    ADMIN;


    fun needCheckUserId(): Boolean {
        return this === USER
    }

}