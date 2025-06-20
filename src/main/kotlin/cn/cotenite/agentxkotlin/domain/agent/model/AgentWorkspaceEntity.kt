package cn.cotenite.agentxkotlin.domain.agent.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
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
@Entity
@Table(name = "agent_workspace")
open class AgentWorkspaceEntity(
    @Id
    open var id: String,

    @Size(max = 36)
    @Convert(disableConversion = true)
    @Column(name = "agent_id", nullable = false, length = 36)
    open var agentId: String,

    @Size(max = 36)
    @Convert(disableConversion = true)
    @Column(name = "user_id", nullable = false, length = 36)
    open var userId: String,

    @Column(name = "created_at", nullable = false)
    open var createdAt: Instant
){

}