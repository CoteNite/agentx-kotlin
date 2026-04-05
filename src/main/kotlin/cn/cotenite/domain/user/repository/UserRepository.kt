package cn.cotenite.domain.user.repository

import org.apache.ibatis.annotations.Mapper
import cn.cotenite.domain.user.model.UserEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository

/**
 * 用户仓储接口
 */
@Mapper
interface UserRepository : MyBatisPlusExtRepository<UserEntity>
