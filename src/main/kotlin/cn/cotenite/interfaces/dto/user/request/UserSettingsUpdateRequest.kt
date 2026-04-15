package cn.cotenite.interfaces.dto.user.request

import cn.cotenite.domain.user.model.config.UserSettingsConfig

data class UserSettingsUpdateRequest(
    var settingConfig: UserSettingsConfig?=null
)