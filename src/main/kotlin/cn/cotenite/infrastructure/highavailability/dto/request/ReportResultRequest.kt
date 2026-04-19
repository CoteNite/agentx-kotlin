package cn.cotenite.infrastructure.highavailability.dto.request

class ReportResultRequest(
    var userId: String? = null,
    var instanceId: String? = null,
    var businessId: String? = null,
    var success: Boolean? = null,
    var latencyMs: Long? = null,
    var errorMessage: String? = null,
    var errorType: String? = null,
    var usageMetrics: Map<String, Any>? = null,
    var callTimestamp: Long? = null
)
