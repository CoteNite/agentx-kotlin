package cn.cotenite.domain.user.model.config

data class UserSettingsConfig(
    var defaultModel: String?=null,
    var fallbackConfig: FallbackConfig?=null
)