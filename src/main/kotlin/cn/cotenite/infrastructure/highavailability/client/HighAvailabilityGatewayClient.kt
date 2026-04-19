package cn.cotenite.infrastructure.highavailability.client

import cn.cotenite.infrastructure.config.HighAvailabilityProperties
import cn.cotenite.infrastructure.exception.BusinessException
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceBatchCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceBatchDeleteRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ApiInstanceUpdateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ProjectCreateRequest
import cn.cotenite.infrastructure.highavailability.dto.request.ReportResultRequest
import cn.cotenite.infrastructure.highavailability.dto.request.SelectInstanceRequest
import cn.cotenite.infrastructure.highavailability.dto.response.ApiInstanceDTO
import cn.cotenite.infrastructure.highavailability.dto.response.GatewayResult
import cn.cotenite.infrastructure.utils.JsonUtils
import org.apache.hc.client5.http.classic.methods.HttpDelete
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.classic.methods.HttpPut
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.io.entity.StringEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.charset.StandardCharsets

/**
 * 高可用网关HTTP客户端
 * 负责与高可用网关进行HTTP通信
 */
@Component
class HighAvailabilityGatewayClient(
    private val properties: HighAvailabilityProperties
) {

    private val logger = LoggerFactory.getLogger(HighAvailabilityGatewayClient::class.java)
    private val httpClient: CloseableHttpClient = HttpClients.createDefault()

    /** 选择最佳API实例 */
    fun selectBestInstance(request: SelectInstanceRequest): ApiInstanceDTO {
        if (!properties.enabled) {
            throw BusinessException("高可用功能未启用")
        }

        try {
            val url = properties.gatewayUrl + "/gateway/select-instance"
            val httpPost = HttpPost(url)
            httpPost.setHeader("Content-Type", "application/json")
            httpPost.setHeader("api-key", properties.apiKey)
            httpPost.entity = StringEntity(JsonUtils.toJsonString(request), ContentType.APPLICATION_JSON)

            httpClient.execute(httpPost).use { response ->
                val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                if (response.code != 200) {
                    logger.error("选择实例失败，响应码: {}, 响应体: {}", response.code, responseBody)
                    throw BusinessException("选择实例失败: $responseBody")
                }

                val rawResult = JsonUtils.parseObject(responseBody, GatewayResult::class.java)
                if (rawResult == null || !rawResult.isSuccess() || rawResult.data == null) {
                    val errorMsg = rawResult?.message ?: "解析响应失败"
                    logger.error("网关返回失败: {}", errorMsg)
                    throw BusinessException("网关返回失败: $errorMsg")
                }

                val dataJson = JsonUtils.toJsonString(rawResult.data)
                val selectedInstance = JsonUtils.parseObject(dataJson, ApiInstanceDTO::class.java)
                if (selectedInstance == null) {
                    logger.error("解析API实例信息失败")
                    throw BusinessException("解析API实例信息失败")
                }

                logger.info("成功选择实例: businessId={}, instanceId={}", selectedInstance.businessId, selectedInstance.id)
                return selectedInstance
            }
        } catch (e: Exception) {
            logger.error("选择API实例失败", e)
            throw BusinessException("选择API实例失败", e)
        }
    }

    /** 上报调用结果 */
    fun reportResult(request: ReportResultRequest) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = properties.gatewayUrl + "/gateway/report-result"
            val httpPost = HttpPost(url)
            httpPost.setHeader("Content-Type", "application/json")
            httpPost.setHeader("api-key", properties.apiKey)
            httpPost.entity = StringEntity(JsonUtils.toJsonString(request), ContentType.APPLICATION_JSON)

            httpClient.execute(httpPost).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.warn("上报调用结果失败，响应码: {}, 响应体: {}", response.code, responseBody)
                }
            }
        } catch (e: Exception) {
            logger.error("上报调用结果失败", e)
            // 上报失败不抛异常，避免影响主流程
        }
    }

    /** 创建API实例 */
    fun createApiInstance(request: ApiInstanceCreateRequest) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = properties.gatewayUrl + "/instances"
            val httpPost = HttpPost(url)
            httpPost.setHeader("Content-Type", "application/json")
            httpPost.setHeader("api-key", properties.apiKey)
            httpPost.entity = StringEntity(JsonUtils.toJsonString(request), ContentType.APPLICATION_JSON)

            httpClient.execute(httpPost).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.error("创建API实例失败，响应码: {}, 响应体: {}", response.code, responseBody)
                    throw BusinessException("创建API实例失败: $responseBody")
                }
            }
        } catch (e: Exception) {
            logger.error("创建API实例失败", e)
            throw BusinessException("创建API实例失败", e)
        }
    }

    /** 更新API实例 */
    fun updateApiInstance(apiType: String, businessId: String, request: ApiInstanceUpdateRequest) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = String.format("%s/instances/%s/%s", properties.gatewayUrl, apiType, businessId)
            val httpPut = HttpPut(url)
            httpPut.setHeader("Content-Type", "application/json")
            httpPut.setHeader("api-key", properties.apiKey)
            httpPut.entity = StringEntity(JsonUtils.toJsonString(request), ContentType.APPLICATION_JSON)

            httpClient.execute(httpPut).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.error("更新API实例失败，响应码: {}, 响应体: {}", response.code, responseBody)
                }
            }
        } catch (e: Exception) {
            logger.error("更新API实例失败", e)
        }
    }

    /** 删除API实例 */
    fun deleteApiInstance(apiType: String, businessId: String) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = String.format("%s/instances/%s/%s", properties.gatewayUrl, apiType, businessId)
            val httpDelete = HttpDelete(url)
            httpDelete.setHeader("api-key", properties.apiKey)

            httpClient.execute(httpDelete).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.error("删除API实例失败，响应码: {}, 响应体: {}", response.code, responseBody)
                }
            }
        } catch (e: Exception) {
            logger.error("删除API实例失败", e)
        }
    }

    /** 启用API实例 */
    fun activateApiInstance(apiType: String, businessId: String) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = String.format("%s/instances/%s/%s/activate", properties.gatewayUrl, apiType, businessId)
            val httpPost = HttpPost(url)
            httpPost.setHeader("Content-Type", "application/json")
            httpPost.setHeader("api-key", properties.apiKey)

            httpClient.execute(httpPost).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.error("启用API实例失败，响应码: {}, 响应体: {}", response.code, responseBody)
                } else {
                    logger.info("API实例启用成功，apiType: {}, businessId: {}", apiType, businessId)
                }
            }
        } catch (e: Exception) {
            logger.error("启用API实例失败", e)
        }
    }

    /** 禁用API实例 */
    fun deactivateApiInstance(apiType: String, businessId: String) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = String.format("%s/instances/%s/%s/deactivate", properties.gatewayUrl, apiType, businessId)
            val httpPost = HttpPost(url)
            httpPost.setHeader("Content-Type", "application/json")
            httpPost.setHeader("api-key", properties.apiKey)

            httpClient.execute(httpPost).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.error("禁用API实例失败，响应码: {}, 响应体: {}", response.code, responseBody)
                } else {
                    logger.info("API实例禁用成功，apiType: {}, businessId: {}", apiType, businessId)
                }
            }
        } catch (e: Exception) {
            logger.error("禁用API实例失败", e)
        }
    }

    /** 创建项目 */
    fun createProject(request: ProjectCreateRequest) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = properties.gatewayUrl + "/projects"
            val httpPost = HttpPost(url)
            httpPost.setHeader("Content-Type", "application/json")
            httpPost.setHeader("api-key", properties.apiKey)
            httpPost.entity = StringEntity(JsonUtils.toJsonString(request), ContentType.APPLICATION_JSON)

            httpClient.execute(httpPost).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.warn("创建项目失败，响应码: {}, 响应体: {}", response.code, responseBody)
                }
            }
        } catch (e: Exception) {
            logger.error("创建项目失败", e)
        }
    }

    /** 批量创建API实例 */
    fun batchCreateApiInstances(instances: List<ApiInstanceCreateRequest>) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = properties.gatewayUrl + "/instances/batch"
            val httpPost = HttpPost(url)
            httpPost.setHeader("Content-Type", "application/json")
            httpPost.setHeader("api-key", properties.apiKey)
            val batchRequest = ApiInstanceBatchCreateRequest(instances)
            httpPost.entity = StringEntity(JsonUtils.toJsonString(batchRequest), ContentType.APPLICATION_JSON)

            httpClient.execute(httpPost).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.error("批量创建API实例失败，响应码: {}, 响应体: {}", response.code, responseBody)
                    throw BusinessException("批量创建API实例失败: $responseBody")
                }

                logger.info("批量创建API实例成功，实例数量: {}", instances.size)
            }
        } catch (e: Exception) {
            logger.error("批量创建API实例失败", e)
            throw BusinessException("批量创建API实例失败", e)
        }
    }

    /** 批量删除API实例 */
    fun batchDeleteApiInstances(instances: List<ApiInstanceBatchDeleteRequest.ApiInstanceDeleteItem>) {
        if (!properties.enabled) {
            return
        }

        try {
            val url = properties.gatewayUrl + "/instances/batch"
            val httpDelete = HttpUriRequestBase("DELETE", URI.create(url))
            httpDelete.setHeader("Content-Type", "application/json")
            httpDelete.setHeader("api-key", properties.apiKey)

            val batchRequest = ApiInstanceBatchDeleteRequest(instances)
            httpDelete.entity = StringEntity(JsonUtils.toJsonString(batchRequest), ContentType.APPLICATION_JSON)

            httpClient.execute(httpDelete).use { response ->
                if (response.code != 200) {
                    val responseBody = EntityUtils.toString(response.entity, StandardCharsets.UTF_8)
                    logger.error("批量删除API实例失败，响应码: {}, 响应体: {}", response.code, responseBody)
                } else {
                    logger.info("批量删除API实例成功，删除数量: {}", instances.size)
                }
            }
        } catch (e: Exception) {
            logger.error("批量删除API实例失败", e)
            // 删除失败不抛异常，避免影响主流程
        }
    }
}
