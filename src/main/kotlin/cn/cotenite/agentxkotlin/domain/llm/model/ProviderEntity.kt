package cn.cotenite.agentxkotlin.domain.llm.model

import cn.cotenite.agentxkotlin.infrastructure.converter.ProviderConfigConverter
import cn.cotenite.agentxkotlin.infrastructure.converter.ProviderProtocolConverter
import cn.cotenite.agentxkotlin.infrastructure.entity.BaseEntity
import cn.cotenite.agentxkotlin.infrastructure.exception.BusinessException
import cn.cotenite.agentxkotlin.infrastructure.llm.protocol.enums.ProviderProtocol
import cn.cotenite.agentxkotlin.domain.llm.model.config.ProviderConfig
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
 * @Date  2025/6/22 03:09
 */
@Entity // 标记这是一个JPA实体
@Table(name = "providers") // 映射到数据库表名
open class ProviderEntity(
    @field:Id // 标记为主键
    @field:GeneratedValue(strategy = GenerationType.UUID) // 使用UUID作为ID生成策略
    @field:Column(name = "id", nullable = false, updatable = false) // 明确列名和属性
    var id: String? = null,

    @field:Column(name = "user_id") // 用户ID可以为空，表示官方提供商
    var userId: String? = null,

    @field:Column(name = "protocol", nullable = false) // 协议类型
    @Convert(converter = ProviderProtocolConverter::class)
    var protocol: ProviderProtocol? = null,

    @field:Column(name = "name", nullable = false, length = 255) // 名称
    var name: String? = null,

    @field:Column(name = "description", length = 512) // 描述
    var description: String? = null,

    @field:Column(name = "config", columnDefinition = "json") // 配置信息
    @Convert(converter = ProviderConfigConverter::class)
    var config: ProviderConfig? = null,

    @field:Column(name = "is_official", nullable = false) // 是否官方
    var isOfficial: Boolean = false, // 默认非官方

    @field:Column(name = "status", nullable = false) // 状态（激活/禁用）
    var status: Boolean = true // 默认激活

) : BaseEntity() { // 继承BaseEntity，BaseEntity应使用 @MappedSuperclass

    // JPA要求实体类有无参构造函数。
    // 如果你使用了 'kotlin-noarg' Gradle 插件并正确配置，通常不需要手动编写。

    // equals() 和 hashCode() 的实现对于JPA实体至关重要
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as ProviderEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "ProviderEntity(id=$id, name='$name', protocol=$protocol, status=$status)"
    }

    /**
     * 检查服务商是否激活
     */
    fun isActive() {
        if (!status) {
            throw BusinessException("服务商未激活") // 确保 BusinessException 已定义并可访问
        }
    }
}