package cn.cotenite.domain.user.repository

import cn.cotenite.domain.user.model.UserSettingsEntity
import cn.cotenite.infrastructure.repository.MyBatisPlusExtRepository
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserSettingsRepository: MyBatisPlusExtRepository<UserSettingsEntity> {
}