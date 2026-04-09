package cn.cotenite.domain.tool.repository

import cn.cotenite.domain.tool.model.UserToolEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository
import org.apache.ibatis.annotations.Mapper

/**
 * @author  yhk
 * Description  
 * Date  2026/4/6 21:52
 */
@Mapper
interface UserToolRepository: MyBatisPlusExtRepository<UserToolEntity> {
}
