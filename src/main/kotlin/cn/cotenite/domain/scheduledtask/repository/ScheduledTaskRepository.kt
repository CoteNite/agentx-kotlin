package cn.cotenite.domain.scheduledtask.repository

import cn.cotenite.domain.scheduledtask.model.ScheduledTaskEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository
import org.apache.ibatis.annotations.Mapper

/**
 * @author  yhk
 * Description  
 * Date  2026/4/9 18:10
 */
@Mapper
interface ScheduledTaskRepository : MyBatisPlusExtRepository<ScheduledTaskEntity>{
}