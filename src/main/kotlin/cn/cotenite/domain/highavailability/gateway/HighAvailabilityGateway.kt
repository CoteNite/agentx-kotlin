package cn.cotenite.domain.highavailability.gateway

import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceBatchDeleteRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceUpdateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ProjectCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ReportResultRequest
import cn.cotenite.infrastructure.highavailability.dto.request.SelectInstanceRequest
import cn.cotenite.infrastructure.highavailability.dto.response.ApiInstanceDTO

/**
 * 高可用网关接口，定义基础设施层需要实现的技术操作。
 *
 * @author yhk
 * @since 1.0.0
 */
interface HighAvailabilityGateway {
    /**
     * 选择最佳实例。
     */
    fun selectBestInstance(request: SelectInstanceRequest): ApiInstanceDTO

    /**
     * 创建 API 实例。
     */
    fun createApiInstance(request: ApiInstanceCreateRequest)

    /**
     * 删除 API 实例。
     */
    fun deleteApiInstance(type: String, businessId: String)

    /**
     * 更新 API 实例。
     */
    fun updateApiInstance(type: String, businessId: String, request: ApiInstanceUpdateRequest)

    /**
     * 上报调用结果。
     */
    fun reportResult(request: ReportResultRequest)

    /**
     * 创建项目。
     */
    fun createProject(request: ProjectCreateRequest)

    /**
     * 批量创建 API 实例。
     */
    fun batchCreateApiInstances(requests: List<ApiInstanceCreateRequest>)

    /**
     * 激活 API 实例。
     */
    fun activateApiInstance(type: String, businessId: String)

    /**
     * 停用 API 实例。
     */
    fun deactivateApiInstance(type: String, businessId: String)

    /**
     * 批量删除 API 实例。
     */
    fun batchDeleteApiInstances(instances: List<ApiInstanceBatchDeleteRequest.ApiInstanceDeleteItem>)
}
