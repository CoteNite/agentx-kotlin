package cn.cotenite.infrastructure.highavailability.gateway

import cn.cotenite.domain.highavailability.gateway.HighAvailabilityGateway
import cn.cotenite.infrastructure.highavailability.client.HighAvailabilityGatewayClient
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceBatchDeleteRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceUpdateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ProjectCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ReportResultRequest
import cn.cotenite.infrastructure.highavailability.dto.request.SelectInstanceRequest
import cn.cotenite.infrastructure.highavailability.dto.response.ApiInstanceDTO
import org.springframework.stereotype.Component

/**
 * 高可用网关基础设施实现
 * 负责所有技术细节，包括HTTP调用、序列化、网络异常处理等
 */
@Component
class HighAvailabilityGatewayImpl(
    private val gatewayClient: HighAvailabilityGatewayClient
) : HighAvailabilityGateway {

    override fun selectBestInstance(request: SelectInstanceRequest): ApiInstanceDTO {
        return gatewayClient.selectBestInstance(request)
    }

    override fun createApiInstance(request: ApiInstanceCreateRequest) {
        gatewayClient.createApiInstance(request)
    }

    override fun deleteApiInstance(type: String, businessId: String) {
        gatewayClient.deleteApiInstance(type, businessId)
    }

    override fun updateApiInstance(type: String, businessId: String, request: ApiInstanceUpdateRequest) {
        gatewayClient.updateApiInstance(type, businessId, request)
    }

    override fun reportResult(request: ReportResultRequest) {
        gatewayClient.reportResult(request)
    }

    override fun createProject(request: ProjectCreateRequest) {
        gatewayClient.createProject(request)
    }

    override fun batchCreateApiInstances(requests: List<ApiInstanceCreateRequest>) {
        gatewayClient.batchCreateApiInstances(requests)
    }

    override fun activateApiInstance(type: String, businessId: String) {
        gatewayClient.activateApiInstance(type, businessId)
    }

    override fun deactivateApiInstance(type: String, businessId: String) {
        gatewayClient.deactivateApiInstance(type, businessId)
    }

    override fun batchDeleteApiInstances(instances: List<ApiInstanceBatchDeleteRequest.ApiInstanceDeleteItem>) {
        gatewayClient.batchDeleteApiInstances(instances)
    }
}
