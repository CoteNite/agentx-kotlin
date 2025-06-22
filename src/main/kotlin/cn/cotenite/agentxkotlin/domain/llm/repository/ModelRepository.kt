package cn.cotenite.agentxkotlin.domain.llm.repository

import cn.cotenite.agentxkotlin.domain.llm.model.ModelEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:19
 */
@Repository
interface ModelRepository : JpaRepository<ModelEntity, String> , JpaSpecificationExecutor<ModelEntity> {

    fun findByProviderId(providerId: String): List<ModelEntity>
    fun findByProviderIdAndStatus(providerId: String, status: Boolean): List<ModelEntity>
    fun findByProviderIdAndUserId(providerId: String, userId: String): List<ModelEntity>
    fun findByProviderIdAndUserIdAndStatus(providerId: String, userId: String, status: Boolean): List<ModelEntity>


}