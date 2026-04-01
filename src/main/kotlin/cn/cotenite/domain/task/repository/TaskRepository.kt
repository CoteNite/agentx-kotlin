package cn.cotenite.domain.task.repository

import cn.cotenite.domain.task.model.TaskEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository
import org.apache.ibatis.annotations.Mapper

/**
 * @author  yhk
 * Description
 * Date  2026/4/1 18:34
 */
@Mapper
interface TaskRepository: MyBatisPlusExtRepository<TaskEntity> {
}