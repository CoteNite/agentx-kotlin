package cn.cotenite.application.user.service

import cn.cotenite.application.user.assembler.UserSettingsAssembler
import cn.cotenite.application.user.dto.UserSettingsDTO
import cn.cotenite.domain.user.service.UserSettingsDomainService
import cn.cotenite.interfaces.dto.user.request.UserSettingsUpdateRequest
import org.springframework.stereotype.Service


@Service
class UserSettingsAppService(
    private val userSettingsDomainService: UserSettingsDomainService
){

    /** * 获取用户设置
     * @param userId 用户ID
     * @return 用户设置DTO
     */
    fun getUserSettings(userId: String): UserSettingsDTO? {
        return UserSettingsAssembler.toDTO(userSettingsDomainService.getUserSettings(userId))
    }

    /** * 更新用户设置
     * @param request 更新请求
     * @param userId 用户ID
     * @return 更新后的用户设置DTO
     */
    fun updateUserSettings(request: UserSettingsUpdateRequest, userId: String): UserSettingsDTO? {
        val entity = UserSettingsAssembler.toEntity(request,userId) ?: return null
        userSettingsDomainService.update(entity)
        return UserSettingsAssembler.toDTO(entity)
    }

    /** * 获取用户默认模型ID
     * @param userId 用户ID
     * @return 默认模型ID
     */
    fun getUserDefaultModelId(userId: String): String? =
        userSettingsDomainService.getUserDefaultModelId(userId)

}