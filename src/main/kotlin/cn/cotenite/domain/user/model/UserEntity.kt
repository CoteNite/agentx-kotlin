package cn.cotenite.domain.user.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import cn.cotenite.infrastructure.entity.BaseEntity
import cn.cotenite.infrastructure.exception.BusinessException

/**
 * 用户实体
 */
@TableName("users")
class UserEntity : BaseEntity() {
    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null
    var nickname: String? = null
    var email: String? = null
    var phone: String? = null
    var password: String? = null
    var githubId: String? = null
    var githubLogin: String? = null
    var avatarUrl: String? = null

    fun valid() {
        if (email.isNullOrBlank() && phone.isNullOrBlank() && githubId.isNullOrBlank()) {
            throw BusinessException("必须使用邮箱、手机号或GitHub账号来作为账号")
        }
    }
}
