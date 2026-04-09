package cn.cotenite.domain.tool.service

import cn.cotenite.domain.tool.model.UserToolEntity
import cn.cotenite.domain.tool.repository.UserToolRepository
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.interfaces.dto.tool.request.QueryToolRequest
import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.springframework.stereotype.Service
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

@Service
class UserToolDomainService(
    private val userToolRepository: UserToolRepository
){

    fun add(userToolEntity: UserToolEntity) {
        userToolRepository.checkInsert(userToolEntity)
    }

    fun listByUserId(userId: String, queryToolRequest: QueryToolRequest): Page<UserToolEntity> {
        val wrapper = KtUpdateWrapper(UserToolEntity::class.java)
            .eq(UserToolEntity::userId, userId)

        // 注意：这里手动处理了 Int 到 Long 的转换，防止之前遇到的 Argument type mismatch 报错
        val page = Page<UserToolEntity>(queryToolRequest.page.toLong(), queryToolRequest.pageSize.toLong())

        return userToolRepository.selectPage(page, wrapper)
    }

    fun findByToolIdAndUserId(toolId: String, userId: String): UserToolEntity? {
        val wrapper = KtUpdateWrapper(UserToolEntity::class.java)
            .eq(UserToolEntity::toolId, toolId)
            .eq(UserToolEntity::userId, userId)

        return userToolRepository.selectOne(wrapper)
    }

    fun update(userToolEntity: UserToolEntity) {
        userToolRepository.checkedUpdateById(userToolEntity)
    }

    fun delete(toolId: String, userId: String) {
        val wrapper = KtUpdateWrapper(UserToolEntity::class.java)
            .eq(UserToolEntity::toolId, toolId)
            .eq(UserToolEntity::userId, userId)

        userToolRepository.checkedDelete(wrapper)
    }

    /**
     * 获取工具的安装次数
     */
    fun getToolsInstall(toolIds: List<String>?): Map<String, Long> {
        if (toolIds.isNullOrEmpty()) {
            return emptyMap()
        }

        val wrapper = KtQueryWrapper(UserToolEntity::class.java).`in`(UserToolEntity::toolId, toolIds)

        val userToolEntities = userToolRepository.selectList(wrapper)

        return userToolEntities.groupingBy { it.toolId?:throw BusinessException("用户下没有工具") }
            .eachCount()
            .mapValues { it.value.toLong() }
    }

    /**
     * 检查工具版本是否已安装
     *
     * @param toolIds 工具版本id列表
     * @param userId 用户id
     */
    fun getInstallTool(toolIds: List<String>?, userId: String): List<UserToolEntity> {
        if (toolIds.isNullOrEmpty()) {
            return emptyList()
        }

        val userToolEntities = userToolRepository.selectList(
            KtQueryWrapper(UserToolEntity::class.java)
                .`in`(UserToolEntity::toolId, toolIds)
                .eq(UserToolEntity::userId, userId)
        )

        // 将查询结果转化为包含所有 toolId 的 Set 提高查找效率
        val foundToolIds = userToolEntities.map { it.toolId }.toSet()

        // 校验：如果输入的某个 toolId 不在查询结果中，抛出异常
        toolIds.forEach { toolId ->
            if (toolId !in foundToolIds) {
                throw BusinessException("使用的工具不存在")
            }
        }

        return userToolEntities
    }
}
