package cn.cotenite.application.tool.assembler

import cn.cotenite.application.tool.dto.ToolDTO
import cn.cotenite.application.tool.dto.ToolVersionDTO
import cn.cotenite.domain.tool.model.ToolEntity
import cn.cotenite.domain.tool.model.ToolVersionEntity
import cn.cotenite.domain.tool.model.UserToolEntity
import cn.cotenite.infrastructure.utils.JsonUtils
import cn.cotenite.interfaces.dto.tool.request.CreateToolRequest
import cn.cotenite.interfaces.dto.tool.request.UpdateToolRequest
import org.springframework.beans.BeanUtils

/**
 * 工具实体转换器
 */
object ToolAssembler {

    /**
     * 将创建工具请求转换为工具实体
     */
    fun toEntity(request: CreateToolRequest, userId: String): ToolEntity {
        return ToolEntity().apply {
            BeanUtils.copyProperties(request, this)
            this.userId = userId
        }
    }

    /**
     * 将工具实体转换为DTO
     */
    fun toDTO(entity: ToolEntity): ToolDTO {
        return ToolDTO().apply {
            BeanUtils.copyProperties(entity, this)
            // 处理复杂字段转换
            installCommand = JsonUtils.toJsonString(entity.installCommand)
        }
    }

    /**
     * 将工具版本实体转换为DTO
     */
    fun toDTO(entity: ToolVersionEntity): ToolVersionDTO {
        return ToolVersionDTO().apply {
            BeanUtils.copyProperties(entity, this)
        }
    }

    /**
     * 将工具实体列表转换为DTO列表
     */
    fun toDTOs(entities: List<ToolEntity>?): List<ToolDTO> {
        return entities?.map { toDTO(it) } ?: emptyList()
    }

    /**
     * 将更新工具请求转换为工具实体
     */
    fun toEntity(request: UpdateToolRequest, userId: String): ToolEntity {
        return ToolEntity().apply {
            BeanUtils.copyProperties(request, this)
            this.userId = userId
        }
    }

    /**
     * 将用户已安装工具实体转换为版本DTO
     */
    fun toDTO(userToolEntity: UserToolEntity): ToolVersionDTO {
        return ToolVersionDTO().apply {
            BeanUtils.copyProperties(userToolEntity, this)
        }
    }
}