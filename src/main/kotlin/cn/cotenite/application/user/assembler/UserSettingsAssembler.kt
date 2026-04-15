package cn.cotenite.application.user.assembler

import cn.cotenite.application.user.dto.UserSettingsDTO
import cn.cotenite.domain.user.model.UserSettingsEntity
import cn.cotenite.interfaces.dto.user.request.UserSettingsUpdateRequest
import org.springframework.beans.BeanUtils

/** 用户设置转换器 */
object UserSettingsAssembler {

    /** 实体转DTO */
    fun toDTO(entity: UserSettingsEntity?): UserSettingsDTO? {
        return entity?.let {
            UserSettingsDTO().apply {
                BeanUtils.copyProperties(it, this)
            }
        }
    }

    /** 请求转实体 */
    fun toEntity(request: UserSettingsUpdateRequest?, userId: String): UserSettingsEntity? {
        return request?.let {
            UserSettingsEntity().apply {
                settingConfig = it.settingConfig
                this.userId = userId
            }
        }
    }
}