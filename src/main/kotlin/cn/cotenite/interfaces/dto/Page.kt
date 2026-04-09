package cn.cotenite.interfaces.dto

/**
 * 分页基础类
 * 使用 open 关键字允许其他 DTO 继承此类
*/
open class Page {

/** 当前页码，默认第一页 */
var page: Int = 1

/** 每页条数，默认 15 条 */
var pageSize: Int = 15

}