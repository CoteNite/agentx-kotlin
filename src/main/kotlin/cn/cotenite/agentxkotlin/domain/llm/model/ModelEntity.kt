package cn.cotenite.agentxkotlin.domain.llm.model

import cn.cotenite.agentxkotlin.domain.llm.model.config.LLMModelConfig
import cn.cotenite.agentxkotlin.domain.llm.model.enums.ModelType
import cn.cotenite.agentxkotlin.infrastructure.converter.LLMModelConfigConverter
import cn.cotenite.agentxkotlin.infrastructure.converter.ModelTypeConverter
import cn.cotenite.agentxkotlin.infrastructure.entity.BaseEntity
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:02
 */
@Entity // 标记这是一个JPA实体
@Table(name = "models") // 映射到数据库表名
open class ModelEntity(
    @field:Id // 标记为主键
    @field:GeneratedValue(strategy = GenerationType.UUID) // 使用UUID作为ID生成策略
    @field:Column(name = "id", nullable = false, updatable = false) // 明确列名和属性，不可为空且不可更新
    var id: String? = null,

    @field:Column(name = "user_id") // 用户ID可以为空，表示官方模型
    var userId: String? = null,

    @field:Column(name = "provider_id", nullable = false) // 供应商ID通常不为空
    var providerId: String? = null,

    @field:Column(name = "model_id", nullable = false) // 模型的唯一标识符
    var modelId: String? = null,

    @field:Column(name = "name", nullable = false, length = 255) // 模型名称
    var name: String? = null,

    @field:Column(name = "description", length = 512) // 描述
    var description: String? = null,

    @field:Column(name = "is_official", nullable = false) // 是否官方模型
    var isOfficial: Boolean = false, // 布尔值通常有默认值

    @field:Column(name = "type", nullable = false) // 模型类型
    @Convert(converter = ModelTypeConverter::class)
    var type: ModelType? = null,

    @field:Column(name = "config", columnDefinition = "json") // 模型配置
    @Convert(converter = LLMModelConfigConverter::class)
    var config: LLMModelConfig? = null, // LLMModelConfig 可能有默认值，或者在这里初始化

    @field:Column(name = "status", nullable = false) // 模型状态（激活/禁用）
    var status: Boolean = true // 默认激活
) : BaseEntity() { // 继承BaseEntity，BaseEntity应使用 @MappedSuperclass


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as ModelEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "ModelEntity(id=$id, name='$name', type=$type, status=$status)"
    }

    /**
     * 检查模型是否激活
     */
    fun isActive() {
        if (!status) {
            throw BusinessException("模型未激活") // 确保 BusinessException 已定义并可访问
        }
    }
}