package cn.cotenite.domain.tool.service.state

import cn.cotenite.domain.tool.constant.ToolStatus
import cn.cotenite.domain.tool.model.ToolEntity

/**
 * @author  yhk
 * Description  
 * Date  2026/4/7 12:01
 */
interface ToolStateProcessor {

    /** 获取处理器对应的状态  */
    fun getStatus(): ToolStatus

    /** 处理工具状态  */
    suspend fun process(tool: ToolEntity)

    /** 获取下一个状态  */
    fun getNextStatus(): ToolStatus?
    
}