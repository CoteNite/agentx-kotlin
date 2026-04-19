package cn.cotenite.infrastructure.highavailability.dto.response

class ApiInstanceDTO(
    var id: String? = null,
    var projectId: String? = null,
    var projectName: String? = null,
    var userId: String? = null,
    var apiIdentifier: String? = null,
    var apiType: String? = null,
    var businessId: String? = null,
    var routingParams: Map<String, Any>? = null,
    var status: String? = null,
    var metadata: Map<String, Any>? = null
)
