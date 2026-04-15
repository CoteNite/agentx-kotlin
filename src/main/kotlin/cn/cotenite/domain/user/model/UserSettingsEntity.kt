package cn.cotenite.domain.user.model

import cn.cotenite.domain.user.model.config.UserSettingsConfig
import cn.cotenite.infrastructure.converter.UserSettingsConfigConverter
import cn.cotenite.infrastructure.entity.BaseEntity
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import org.apache.ibatis.type.JdbcType

/** 用户设置领域模型 */
@TableName("user_settings")
class UserSettingsEntity : BaseEntity() {

    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null

    /** 用户ID */
    var userId: String? = null

    /** 设置配置 */
    @TableField(typeHandler = UserSettingsConfigConverter::class, jdbcType = JdbcType.OTHER)
    var settingConfig: UserSettingsConfig? = null

    /** * 获取或设置默认模型ID
     * 使用 Kotlin 的属性访问器（Custom Getter/Setter）替代 Java 的显式方法
     */
    var defaultModelId: String?
        get() = settingConfig?.defaultModel
        set(value) {
            if (settingConfig == null) {
                settingConfig = UserSettingsConfig()
            }
            settingConfig?.defaultModel = value
        }
}