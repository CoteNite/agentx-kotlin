package cn.cotenite.agentxkotlin.domain.agent.model

import cn.cotenite.agentxkotlin.infrastructure.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/20 23:10
 */
@Entity // 标记这是一个JPA实体
@Table(name = "agent_workspace") // 映射到数据库表名
open class AgentWorkspaceEntity(

    /**
     * 主键ID
     */
    @field:Id // 标记为主键
    @field:GeneratedValue(strategy = GenerationType.UUID) // 使用UUID作为ID生成策略
    @field:Column(name = "id", nullable = false, updatable = false) // 明确列名和属性
    var id: String? = null,

    /**
     * Agent ID
     */
    @field:Column(name = "agent_id", nullable = false)
    var agentId: String? = null,

    /**
     * 用户ID
     */
    @field:Column(name = "user_id", nullable = false)
    var userId: String? = null,

    /**
     * 模型id
     */
    @field:Column(name = "model_id")
    var modelId: String? = null
) : BaseEntity() { // 继承BaseEntity，BaseEntity应使用 @MappedSuperclass

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as AgentWorkspaceEntity
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "AgentWorkspaceEntity(id=$id, agentId='$agentId', userId='$userId')"
    }
}