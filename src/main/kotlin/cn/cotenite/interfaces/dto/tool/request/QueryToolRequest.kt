package cn.cotenite.interfaces.dto.tool.request

import cn.cotenite.interfaces.dto.Page

/**
 * @author  yhk
 * Description  
 * Date  2026/4/8 17:33
 */
class QueryToolRequest : Page() {

    /** 工具名称，支持模糊查询 */
    var toolName: String? = null

}