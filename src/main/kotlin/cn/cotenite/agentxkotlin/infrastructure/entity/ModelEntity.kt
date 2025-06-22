package cn.cotenite.agentxkotlin.infrastructure.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime



/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 02:11
 */
@MappedSuperclass // 表示该类是一个实体映射的超类，其属性会被其子实体继承到数据库表中
open class BaseEntity {

    // 创建时间
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    @PrePersist // 在实体持久化（插入）之前调用
    protected fun onCreate() {
        createdAt = LocalDateTime.now()
    }

    // 更新时间
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null

    @PreUpdate // 在实体更新之前调用
    protected fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

    // 操作者类型，不映射到数据库表
    @Transient // 表示该字段不持久化到数据库
    private var operatedBy: Operator = Operator.USER

    fun setAdmin() {
        this.operatedBy = Operator.ADMIN
    }

    fun needCheckUserId(): Boolean {
        return this.operatedBy == Operator.USER
    }
}