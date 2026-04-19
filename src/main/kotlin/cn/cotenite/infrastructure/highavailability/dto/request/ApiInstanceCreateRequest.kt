package cn.cotenite.infrastructure.highavailability.dto.request

class ApiInstanceCreateRequest(
    var userId: String? = null,
    var apiIdentifier: String? = null,
    var apiType: String? = null,
    var businessId: String? = null
)
