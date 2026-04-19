package cn.cotenite.domain.user.service

import cn.cotenite.domain.user.model.UserSettingsEntity
import cn.cotenite.domain.user.repository.UserSettingsRepository
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import org.springframework.stereotype.Service

/** 用户设置领域服务 */
@Service
class UserSettingsDomainService(
    private val userSettingsRepository: UserSettingsRepository
) {

    /** 获取用户设置
     * @param userId 用户ID
     * @return 用户设置实体 */
    fun getUserSettings(userId: String): UserSettingsEntity {
        val wrapper = KtQueryWrapper(UserSettingsEntity::class.java)
            .eq(UserSettingsEntity::userId, userId)
        return userSettingsRepository.selectOne(wrapper)
    }

    /** 更新用户设置（不存在则创建）
     * @param userSettings 用户设置实体 */
    fun update(userSettings: UserSettingsEntity) {
        val existing = KtQueryWrapper(UserSettingsEntity::class.java)
            .eq(UserSettingsEntity::userId, userSettings.userId)
            .let { userSettingsRepository.selectOne(it) }

        if (existing == null) {
            userSettingsRepository.checkInsert(userSettings)
        } else {
            val wrapper = KtQueryWrapper(UserSettingsEntity::class.java)
                .eq(UserSettingsEntity::userId, userSettings.userId)
            userSettingsRepository.checkedUpdate(userSettings, wrapper)
        }
    }

    /** 获取用户默认模型ID
     * @param userId 用户ID
     * @return 默认模型ID */
    fun getUserDefaultModelId(userId: String): String? = getUserSettings(userId).defaultModelId

    /** 获取用户降级链配置
     * @param userId 用户ID
     * @return 降级模型ID列表，如果未启用降级则返回null
     */
    fun getUserFallbackChain(userId: String): MutableList<String> {
        val settings: UserSettingsEntity = getUserSettings(userId)
        if (settings.settingConfig == null) {
            return ArrayList()
        }

        val fallbackConfig= settings.settingConfig?.fallbackConfig
        if (fallbackConfig == null || !fallbackConfig.enabled || fallbackConfig.fallbackChain.isEmpty()) {
            return ArrayList()
        }

        return fallbackConfig.fallbackChain
    }
}