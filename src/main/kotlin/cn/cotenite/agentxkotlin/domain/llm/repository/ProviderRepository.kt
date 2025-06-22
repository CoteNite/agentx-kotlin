package cn.cotenite.agentxkotlin.domain.llm.repository

import cn.cotenite.agentxkotlin.domain.llm.model.ProviderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * @Author  RichardYoung
 * @Description
 * @Date  2025/6/22 03:19
 */
@Repository
interface ProviderRepository: JpaRepository<ProviderEntity, String>, JpaSpecificationExecutor<ProviderEntity>{

    fun findByUserId(userId: String): List<ProviderEntity>
    fun findByIsOfficial(isOfficial: Boolean): List<ProviderEntity>
    fun findByUserIdAndIsOfficial(userId: String, isOfficial: Boolean): List<ProviderEntity>


}