package cn.cotenite.infrastructure.highavailability.dto.request

class ApiInstanceUpdateRequest(
    var userId: String? = null,
    var apiIdentifier: String? = null,
    var routingParams: Map<String, Any>? = null,
    var metadata: Map<String, Any>? = null
)
