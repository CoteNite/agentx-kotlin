package cn.cotenite.application.user.dto

import cn.cotenite.domain.user.model.config.UserSettingsConfig

/** 用户设置数据传输对象 */
data class UserSettingsDTO(
    /** 主键ID */
    var id: String? = null,

    /** 用户ID */
    var userId: String? = null,

    /** 配置 */
    var settingConfig: UserSettingsConfig? = null
)